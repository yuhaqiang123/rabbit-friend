package com.muppet.rabbitfriend.core;

import com.muppet.util.DateUtils;
import com.muppet.util.GsonUtil;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.AMQP.BasicProperties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by yuhaiqiang on 2018/6/27.
 *
 * @description
 */
public class RpcProducer extends BaseProducer implements MessageProducerExtractor {

    private ConcurrentHashMap<String, Envelope> asyncCallback = new ConcurrentHashMap<>();

    private ConcurrentHashMap<String, ConcurrentHashMap<String, Envelope>> replyEnvelopes = new ConcurrentHashMap<>();

    private String replyTo;

    private BaseQueue replyToQueue;

    private BaseExchange exchange;

    private Logger logger = LogManager.getLogger(this.getClass());

    public RpcProducer(RabbitContext context) {
        super(context);
    }


    public RabbitContext getContext() {
        return context;
    }

    public RpcProducer setContext(RabbitContext context) {
        this.context = context;
        return this;
    }

    @Override
    public void start() {
        super.start();
        replyTo = context.getDefaultReplyQueue();

        replyToQueue = new BaseQueue();
        replyToQueue.setName(replyTo);
        context.declareQueue(replyToQueue);

        context.bind(exchange, replyToQueue, new RoutingKey(replyTo));

        logger.info("register cosumer");
        context.registerConsumer(new BaseConsumer(context) {
            @Override
            protected String getQueueName() {
                return replyTo;
            }

            private void nack(Message message) {
                message.nack(false);
            }

            private MessageReply castMessageReply(Message message) {
                try {
                    return message.cast();
                } catch (Throwable throwable) {
                    logger.error("can not cast message[{}] to message reply", message.getClass().getName());
                    nack(message);
                    throw throwable;
                }
            }

            @Override
            public void handle(Message message) {
                MessageReply reply = castMessageReply(message);
                Envelope envelope = replyEnvelopes.get(replyTo).get(reply.getCorrelationId());
                if (envelope == null) {
                    logger.error("receive message reply ,but not found callback for message[{}], drop it and nack(no requeue)[{}]", reply.getCorrelationId(), GsonUtil.toDefaultJson(reply));
                    nack(message);
                    return;
                }
                envelope.ack(reply);
                reply.ack();
            }
        }.setExceptionHandler((message, throwable) -> {
            message.nack(false);
            throw new RabbitFriendException("throw exception when handle message reply, nack(no requeue) this reply", throwable);
        }));
        synchronized (replyEnvelopes) {
            replyEnvelopes.put(replyTo, new ConcurrentHashMap<String, Envelope>());
        }

        //定义默认的replyTo ，作为不带repLyTo字段的NeedReplyMessage的消息,接受回复消息的字段
    }


    @Override
    public void extracte(Message message) {
        if (message instanceof NeedReplyMessage) {
            evalateBasicProperties(message.cast());
        }
    }

    @Override
    public void destroy() {
        context.getDelegateFactory().releaseDelegate(delegate);
    }

    @Override
    public RoutingKey getDefaultRoutingkey() {
        return null;
    }

    /**
     * 如果NeedReplyMessage#getBasicProperties返回非空，实际则不处理
     *
     * @param message
     */
    protected void evalateBasicProperties(NeedReplyMessage message) {
        BasicProperties properties = message.getBasicProperties();
        String expirationTime = null;
        if (message instanceof RetriableMessage) {
            expirationTime = ((RetriableMessage) message).getRetryInterval().toString();
        } else if (message instanceof DefferedMessage) {
            expirationTime = ((DefferedMessage) message).getDefferedTime().toString();
        }
        String messageId = message.getId();
        if (messageId == null) {
            messageId = uuidGenerate.getUuid();
            message.setId(messageId);
        }
        String replyTo = null;
        if (message.getReplyTo() != null) {
            replyTo = message.getReplyTo();
            synchronized (replyEnvelopes) {
                replyEnvelopes.putIfAbsent(replyTo, new ConcurrentHashMap<>());
            }
        } else {
            replyTo = getReplyTo();
        }
        if (replyTo == null) {
            throw new RabbitFriendException("NeedReplyMessage must have replyTo queue, or it's can not be replyed");
        }

        message.setHeaderEntry(Constants.HEADER_EXCHANGE_NAME, getExchange().getName())
                .put(message.TIMEOUT_KEY, message.getTimeout().toString());
        if (properties == null) {
            AMQP.BasicProperties.Builder builder = new AMQP.BasicProperties.Builder();
            builder.messageId(messageId)
                    .correlationId(messageId)
                    .deliveryMode(message.isPersistent() == true ? 2 : 1)
                    .priority(message.getPriority())
                    .expiration(expirationTime)
                    .timestamp(Calendar.getInstance().getTime())
                    .replyTo(replyTo)
                    .headers(message.getHeaders());
            properties = builder.build();
        }
        message.setBasicProperties(properties);

        postEvalateBasicProperties(message, properties);
    }

    protected Envelope getEnvelope(String messageId) {
        return null;
    }

    protected void postEvalateBasicProperties(NeedReplyMessage message, BasicProperties properties) {
    }

    public void send(NeedReplyMessage message) {
        send(message, null);
    }


    public void send(NeedReplyMessage message, AsyncMessageReplyCallback callback) {
        //evalateBasicProperties(message);
        String correlationId = message.getId();
        Envelope envelope = null;
        if (callback != null) {
            ConcurrentHashMap<String, Envelope> envelopes = replyEnvelopes.get(message.getBasicProperties().getReplyTo());
            envelope = new Envelope() {
                AtomicBoolean called = new AtomicBoolean(false);
                AtomicBoolean started = new AtomicBoolean(false);
                private Envelope self = this;

                Timer timer = new Timer();


                void start() {
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            self.timeout();
                        }
                    }, message.getTimeout());//TODO timeout时间不能为负值
                    started.compareAndSet(false, true);
                }


                @Override
                void ack(MessageReply reply) {
                    if (!called.compareAndSet(false, true)) {
                        return;
                    }
                    if (started.compareAndSet(true, true)) {
                        timer.cancel();
                    }
                    envelopes.remove(correlationId);

                    try {
                        callback.run(reply);
                    } catch (Throwable throwable) {
                        throw throwable;
                    } finally {
                        reply.nack(false);
                    }

                }

                @Override
                void timeout() {
                    if (!called.compareAndSet(false, true)) {
                        return;
                    }
                    envelopes.remove(correlationId);
                    MessageReply reply = new MessageReply() {{
                        success = false;
                        error = new ErrorCode() {
                            @Override
                            public String getErrorInfo() {
                                String timeStr = DateUtils.format(message.getBasicProperties().getTimestamp());
                                return String.format("message[%s] timeout after [%s] ms since [%s]", message.getId(), message.getTimeout(), timeStr);
                            }
                        };
                    }};
                    //无论Reply消息处理失败还是是失败，都要对其进行确认，如果用户不想ack，可以在之前调用reply.nack ，作特别的处理
                    try {
                        callback.run(reply);
                    } catch (Throwable throwable) {
                        throw throwable;
                    } finally {
                        reply.nack(false);
                    }
                }

                @Override
                List<Message> getRequests() {
                    return Collections.singletonList(message);
                }
            };

            envelopes.put(correlationId, envelope);

        }
        delegate.safeSend(message, getExchange());

        /**
         * 确保消息发送完成之后再开启timer
         */
        if (envelope != null) {
            envelope.start();
        }

    }

    public void send(List<NeedReplyMessage> messages, AsyncMessageReplyListCallback callback) {
        if (messages == null) {
            return;
        }
        List<MessageReply> replies = new ArrayList<>(messages.size());

        for (NeedReplyMessage message : messages) {
            send(message, new AsyncMessageReplyCallback(null) {
                @Override
                public void run(MessageReply r) {
                    try {
                        callback.run(r);
                    } catch (Throwable t) {
                        //TODO 这块还用不用try catch
                        callback.error(new ErrorCode().setErrorInfo(t.getMessage()));
                    }

                    boolean done = false;
                    synchronized (replies) {
                        replies.add(r);
                        done = true;
                    }
                    if (done == true && replies.size() == messages.size()) {
                        callback.done(replies);
                    }
                }
            });
        }
    }

    public MessageReply call(NeedReplyMessage message) {
        MessageReply[] replies = new MessageReply[1];
        Object lock = new Object();
        send(message, new AsyncMessageReplyCallback((null)) {
            @Override
            public void run(MessageReply r) {
                synchronized (lock) {
                    replies[0] = r;
                    lock.notify();
                }
            }
        });
        synchronized (lock) {
            try {
                //Mesage会有代答机制，所以此处可以不用有限等待，但是避免异常情况，设置等待时间
                //为超时时间的2倍，确保此方法不会因为等待超时抛出异常。如果消息处理超时，则返回超时的MessageReply
                lock.wait(message.getTimeout() * 2);
                return replies[0];
            } catch (Throwable throwable) {
                throw new RabbitFriendException(throwable);
            }
        }
    }

    @Override
    public String getName() {
        return null;
    }


    public String getReplyTo() {
        return replyTo;
    }

    public RpcProducer setReplyTo(String replyTo) {
        this.replyTo = replyTo;
        return this;
    }

    public RpcProducer setExchange(BaseExchange exchange) {
        this.exchange = exchange;
        return this;
    }

    public BaseExchange getExchange() {
        return exchange;
    }

    public RoutingKey getRoutingKey() {
        return null;
    }


}
