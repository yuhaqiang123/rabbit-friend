package com.muppet.rabbitfriend.core;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConfirmListener;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ShutdownSignalException;
import com.rabbitmq.client.impl.recovery.AutorecoveringConnection;
import com.rabbitmq.client.impl.recovery.RecoveryAwareAMQConnection;
import org.apache.commons.lang.reflect.FieldUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

/**
 * Created by yuhaiqiang on 2018/6/28.
 *
 * @description
 */
public class RabbitmqDelegate {


    private Channel defaultChannel;

    private RabbitConfiguration configuration;

    private Connection connection;

    private RabbitContext context;

    private Logger logger = LogManager.getLogger(this.getClass());


    public RabbitmqDelegate(RabbitContext context, Connection connection, Channel channel) {
        this.defaultChannel = channel;
        this.connection = connection;
        this.context = context;
        this.configuration = context.getConfiguration();
    }


    public boolean exchangeExist(String name) {
        return channelExecute(channel -> {
            try {
                channel.exchangeDeclarePassive(name);
                return false;
            } catch (Exception e) {
                return false;
            }
        });
    }

    public void declareQueue(BaseQueue queue) {
        channelExecute(channel -> {
            try {
                /*if (delegate.queueExist(queue.getName())) {
                    return queue;
                }*/
                channel.queueDeclare(queue.getName(), queue.getDurable(), queue.getExclusize(), queue.getAutoDelete(), queue.getArguments());
                return queue;
            } catch (Exception e) {
                throw new RabbitFriendException(e);
            }
        });
    }


    public boolean queueExist(String name) {
        return channelExecute(channel -> {
            try {
                channel.queueDeclarePassive(name);
                return false;
            } catch (Exception e) {
                return false;
            }
        });
    }

    public void declareQueueIfAbsent(BaseQueue queue) {
        //if (!queueExist(queue.getName())) {
        declareQueue(queue);
        //}
    }

    public void safeSend(Message message, BaseExchange baseExchange) {
        new RecoverableSensder(message, baseExchange).send();
    }

    private byte[] getData(Message message) {
        return context.getDefaultMessageConvertor().dump(message);
    }


    public void simpleSend(Message message, BaseExchange baseExchange) {
        channelExecute(channel -> {
            try {
                //message.getHeaders()
                channel.basicPublish(baseExchange.getName(), message.getRoutingkey(), message.getBasicProperties(), getData(message));
            } catch (IOException e) {
                throw new RabbitFriendException(e);
            }
            return null;
        });
    }


    class AsyncConfirmSafePublish {

        private Long maxTimeout = 1000 * 60 * 10L;

        private Channel channel;

        private SortedSet<DeliverTagSendfTuple> waitForConfirms;

        private volatile long lastRemoveTime;
        private volatile long lastAddTime;

        private ReentrantLock lock;

        private Condition isEmpty;

        class DeliverTagSendfTuple {

            public DeliverTagSendfTuple(Long deliverTag, Function<Channel, Void> sendf) {
                this.deliverTag = deliverTag;
                this.sendf = sendf;
            }

            public Long deliverTag;

            public Function<Channel, Void> sendf;

            public boolean equals(Object o) {
                if (deliverTag.equals(o)) {
                    return true;
                }
                return false;
            }

        }


        public AsyncConfirmSafePublish(Channel channel) {
            this.channel = channel;
        }

        public SortedSet<DeliverTagSendfTuple> getWaitForConfirms() {
            return waitForConfirms;
        }

        public AsyncConfirmSafePublish setWaitForConfirms(SortedSet<DeliverTagSendfTuple> waitForConfirms) {
            this.waitForConfirms = waitForConfirms;
            return this;
        }


        private Thread backendCheck = new Thread(() -> {
            while (true) {
                long currentTime = System.currentTimeMillis();
                if ((currentTime - lastRemoveTime) - (lastRemoveTime - lastAddTime) > maxTimeout && !waitForConfirms.isEmpty()) {
                    //检查超时
                    SortedSet<DeliverTagSendfTuple> retrySet = waitForConfirms;
                    waitForConfirms.clear();
                    retrySet.forEach((tuple) -> safePublish(tuple.sendf));
                }
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                    throw new RabbitFriendException(e);
                }
            }
        });

        private void confirmAck(long deliveryTag, boolean multiple) {
            if (multiple) {
                waitForConfirms.headSet(new DeliverTagSendfTuple(deliveryTag + 1, null)).clear();
            } else {
                waitForConfirms.remove(new DeliverTagSendfTuple(deliveryTag, null));
            }
            lastRemoveTime = System.currentTimeMillis();
        }

        public void start() {
            try {
                lock = new ReentrantLock();
                isEmpty = lock.newCondition();
                waitForConfirms = new TreeSet<>(new Comparator<DeliverTagSendfTuple>() {
                    @Override
                    public int compare(DeliverTagSendfTuple o1, DeliverTagSendfTuple o2) {
                        return o1.deliverTag < o2.deliverTag ? -1 : 1;
                    }
                });
                waitForConfirms = Collections.synchronizedSortedSet(waitForConfirms);

                channel.confirmSelect();
                channel.addConfirmListener(new ConfirmListener() {
                    public void handleAck(long deliveryTag, boolean multiple) throws IOException {
                        confirmAck(deliveryTag, multiple);
                    }

                    public void handleNack(long deliveryTag, boolean multiple) throws IOException {
                        confirmAck(deliveryTag, multiple);
                    }
                });
                backendCheck.start();
            } catch (IOException e) {
                throw new RabbitFriendException(e);
            }
        }

        public void safePublish(Function<Channel, Void> sendFunction) {
            Long seqNo = channel.getNextPublishSeqNo();
            sendFunction.apply(channel);
            waitForConfirms.add(new DeliverTagSendfTuple(seqNo, sendFunction));
            lastAddTime = System.currentTimeMillis();
        }

        public Long getMaxTimeout() {
            return maxTimeout;
        }

        public AsyncConfirmSafePublish setMaxTimeout(Long maxTimeout) {
            this.maxTimeout = maxTimeout;
            return this;
        }
    }


    class TransactionalSafePublish {

        private Channel channel;


        public TransactionalSafePublish(Channel channel) {
            this.channel = channel;
        }

        private Integer retryTimes = 5;

        public Integer getRetryTimes() {
            return retryTimes;
        }

        public TransactionalSafePublish setRetryTimes(Integer retryTimes) {
            this.retryTimes = retryTimes;
            return this;
        }

        public void safePublish(Function<Channel, Void> sendFunction) {
            safePublish(sendFunction, retryTimes);
        }

        /**
         * 事务模式提交
         *
         * @param sendFunction
         */
        public void safePublish(Function<Channel, Void> sendFunction, Integer retryTimes) {
            Integer count = retryTimes;
            while (count-- > 0) {
                try {
                    channel.txSelect();
                    try {
                        sendFunction.apply(channel);
                    } catch (Exception e) {
                        channel.txRollback();
                        throw new RabbitFriendException(e);
                    }
                    channel.txCommit();
                    break;
                } catch (IOException e) {
                    try {
                        channel.txRollback();
                    } catch (IOException e1) {
                        throw new RabbitFriendException(e1);
                    }
                }
            }
        }

    }

    public <T> T channelExecute(Function<Channel, T> function) {
        try {
            //TODO 使用的默认头配置
            T t = function.apply(defaultChannel);
            return t;
        } catch (RabbitFriendException e) {
            throw e;
        } finally {
        }
    }


    public class RecoverableSensder {

        private Message message;

        private BaseExchange exchange;

        public RecoverableSensder(Message message, BaseExchange exchange) {
            this.message = message;
            this.exchange = exchange;
        }

        void send() {
            channelExecute((channel) -> {
                try {
                    channel.basicPublish(exchange.getName(), message.getRoutingkey(), true, message.getBasicProperties(), getData(message));
                } catch (ShutdownSignalException e) {
                    if (!(connection instanceof AutorecoveringConnection) || configuration.getIps().length <= 1) {
                        // the connection is not recoverable
                        throw e;
                    }

                    logger.error("failed to send a message because {}; as the connection is recoverable," +
                            "we are doing recoverable send right now", e.getMessage());

                    if (!recoverSend()) {
                        throw e;
                    }
                } catch (IOException e) {
                    throw new RabbitFriendException(e);
                }
                return null;
            });
        }

        private boolean recoverSend() {
            int interval = connection.getHeartbeat() / 2;
            interval = interval > 0 ? interval : 1;
            AtomicInteger count = new AtomicInteger();

            // as the connection is lost, there is no need to wait heart beat missing 8 times
            // so we use reflection to fast the process

            try {
                RecoveryAwareAMQConnection delegate = (RecoveryAwareAMQConnection) FieldUtils.readDeclaredField(connection, "delegate");
                Field _missedHeartbeats = FieldUtils.getField(RecoveryAwareAMQConnection.class, "_missedHeartbeats");
                _missedHeartbeats.setAccessible(true);
                _missedHeartbeats.set(delegate, 100);
            } catch (IllegalAccessException e) {
                throw new RabbitFriendException(e);
            }

            //TODO 重试次数可配置
            while (count.get() < 5) {
                try {
                    TimeUnit.SECONDS.sleep(interval);
                } catch (InterruptedException e1) {
                    throw new RabbitFriendException(e1);
                }

                channelExecute(channel -> {
                    try {
                        channel.basicPublish(exchange.toString(), message.getRoutingkey(), true, message.getBasicProperties(), getData(message))
                        ;
                        return true;
                    } catch (ShutdownSignalException | IOException e) {
                        logger.error("failed to publish message to exchange[{}] ,routing key is[{}]", exchange.getName(), message.getRoutingkey());
                        count.incrementAndGet();
                    }
                    return null;
                });
            }

            return false;
        }
    }

    public Channel getDefaultChannel() {
        return defaultChannel;
    }

    public RabbitmqDelegate setDefaultChannel(Channel defaultChannel) {
        this.defaultChannel = defaultChannel;
        return this;
    }
}
