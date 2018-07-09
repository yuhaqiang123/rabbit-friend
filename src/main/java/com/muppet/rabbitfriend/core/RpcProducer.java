package com.muppet.rabbitfriend.core;

import com.muppet.util.DateUtils;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
public class RpcProducer extends BaseProducer implements Producer {

    private RabbitContext context;

    private ConcurrentHashMap<String, Envelope> asyncCallback = new ConcurrentHashMap<>();

    private ConcurrentHashMap<String, ConcurrentHashMap<String, Envelope>> replyEnvelopes = new ConcurrentHashMap<>();

    private UuidGenerate uuidGenerate;

    private String replyTo;

    private BaseQueue replyToQueue;

    private BaseExchange exchange;

    private RabbitmqDelegate delegate;

    public BaseExchange getExchange() {
        return exchange;
    }

    public RoutingKey getRoutingKey() {
        return null;
    }

    public RpcProducer(RabbitContext context) {
        this.context = context;
    }

    private Logger logger = LogManager.getLogger(this.getClass());

    @Override
    public void start() {
        uuidGenerate = context.getConfiguration().getUuidGenerator();
        delegate = context.getDelegateFactory().acquireDelegate();
        replyTo = context.getDefaultReplyQueue();
        //delegate.

        replyToQueue = new BaseQueue();
        replyToQueue.setName(replyTo);
        delegate.declareQueueIfAbsent(replyToQueue);


        context.bind(exchange, replyToQueue, new RoutingKey(replyTo));

        context.registerConsumer(new BaseConsumer(context) {
            @Override
            protected String getQueueName() {
                return replyTo;
            }

            @Override
            public void handle(Message message) {
                MessageReply reply = message.cast();
                Envelope envelope = replyEnvelopes.get(replyTo).get(reply.getCorrelationId());
                envelope.ack(reply);
                reply.ack();
            }
        });
        synchronized (replyEnvelopes) {
            replyEnvelopes.put(replyTo, new ConcurrentHashMap<String, Envelope>());
        }

        //定义默认的replyTo ，作为不带repLyTo字段的NeedReplyMessage的消息,接受回复消息的字段
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

        if (properties == null) {
            AMQP.BasicProperties.Builder builder = new AMQP.BasicProperties.Builder();
            builder.messageId(messageId)
                    .correlationId(messageId)
                    .deliveryMode(message.isPersistent() == true ? 2 : 1)
                    .priority(message.getPriority())
                    .expiration(expirationTime)
                    .timestamp(Calendar.getInstance().getTime())
                    .replyTo(replyTo)
                    .headers(message.setHeaderEntry(Producer.HEADER_EXCHANGE_NAME, getExchange().getName()));
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


    public void send(NeedReplyMessage message, AsyncSafeCallback callback) {
        evalateBasicProperties(message);
        String correlationId = message.getId();
        ConcurrentHashMap<String, Envelope> envelopes = replyEnvelopes.get(message.getBasicProperties().getReplyTo());
        Envelope envelope = new Envelope() {
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
                callback.run(reply);
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
                callback.run(reply);
            }

            @Override
            List<Message> getRequests() {
                return Collections.singletonList(message);
            }
        };

        envelopes.put(correlationId, envelope);
        delegate.safeSend(message, getExchange());
        envelope.start();
    }


    public void send(NeedReplyMessage message) {

    }

    public void call(NeedReplyMessage message) {

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

    @Override
    public Channel getChannel() {
        return delegate.getDefaultChannel();
    }

    public RpcProducer setChannel(Channel channel) {
        this.delegate.getDefaultChannel();
        return this;
    }
}
