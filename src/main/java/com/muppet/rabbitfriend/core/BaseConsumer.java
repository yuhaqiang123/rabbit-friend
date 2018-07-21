package com.muppet.rabbitfriend.core;

import com.google.gson.Gson;
import com.muppet.util.AspectAddPropertyUtil;
import com.muppet.util.DateUtils;
import com.muppet.util.ExceptionDSL;
import com.muppet.util.GsonUtil;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;
import org.apache.commons.lang.reflect.FieldUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;

/**
 * Created by yuhaiqiang on 2018/7/4.
 *
 * @description
 */
public abstract class BaseConsumer implements Consumer, Lifecycle, Consume, Consume.AutoAckEnable, RabbitFriendComponent {

    protected RabbitContext context;

    private Logger logger = LogManager.getLogger(this.getClass());

    private HashMap<String, RpcProducer> rpcProducers = new HashMap<>();

    private Gson gson = GsonUtil.getGson();

    protected Channel channel;

    protected UuidGenerate uuidGenerate;

    protected RabbitmqDelegate delegate;

    private RpcProducer rpcProducer;

    private java.util.function.BiConsumer<Message, Throwable> exceptionHandler;

    private List<MessageConsumerExtractor> extractors = new ArrayList<>();


    public BaseConsumer(RabbitContext context) {
        this.context = context;
    }

    public BaseConsumer setChannel(Channel channel) {
        this.channel = channel;
        return this;
    }

    public RpcProducer getRpcProducer() {
        return rpcProducer;
    }

    public BaseConsumer setRpcProducer(RpcProducer rpcProducer) {
        this.rpcProducer = rpcProducer;
        return this;
    }

    private Map<String, String> headers = new HashMap<>();

    private AtomicBoolean started = new AtomicBoolean(false);

    @Override
    public void start() {
        if (!started.compareAndSet(false, true)) {
            return;
        }
        uuidGenerate = context.getConfiguration().getUuidGenerator();
        context.declareQueue(new BaseQueue(getQueueName()));
        if (getPreFetchSize() != null) {
            delegate.basicQos(getPreFetchSize(), false);
        }
    }

    protected void initializeDelegate() {
        delegate = context.getDelegateFactory().acquireDelegate();
    }


    protected boolean checkMessage(Message message) {
        if (message instanceof TimeoutMessage) {
            TimeoutMessage timeoutMessage = (TimeoutMessage) message;
            timeoutMessage.getTimeout();
            Date createDate = message.getBasicProperties().getTimestamp();
            Long timeout = Long.valueOf(message.getBasicProperties().getHeaders().get(Constants.HEADER_TIMEOUT_KEY).toString());
            AspectAddPropertyUtil.addGetTimeoutAspect(timeoutMessage, timeout);
            if (timeoutMessage.getTimeout() <= System.currentTimeMillis() - createDate.getTime()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
        Message message = context.getDefaultMessageConvertor().loads(consumerTag, envelope, properties, body);
        message.setBasicProperties(properties);

        boolean isTimeout = checkMessage(message);

        if (isTimeout) {
            //TODO reply the error MessageReply
            logger.error("drop the message[{}], due to it's timeout[createTime:{}, timeout[{}] millseconds"
                    , GsonUtil.toDefaultJson(message)
                    , DateUtils.format(message.getBasicProperties().getTimestamp())
                    , message.getBasicProperties().getHeaders().get(Constants.HEADER_TIMEOUT_KEY));
            channel.basicNack(envelope.getDeliveryTag(), false, false);
            return;
        }
        AtomicBoolean acked = new AtomicBoolean(false);
        BiFunction<Boolean, Boolean, Void> ackFunc = ((ack, requeue) -> {
            if (!acked.compareAndSet(false, true)) {
                return null;
            }

            if (autoAck()) {
                return null;
            }
            try {
                if (ack) {
                    channel.basicAck(envelope.getDeliveryTag(), false);
                } else {
                    //TODO requeue
                    channel.basicNack(envelope.getDeliveryTag(), true, requeue);
                }
            } catch (IOException e) {
                throw new RabbitFriendException(e);
            }
            return null;
        });
        ExceptionDSL.throwable(() -> FieldUtils.writeField(message, "ackFunc", ackFunc, true));

        extractors.stream().forEach((extractor) -> extractor.extracte(message));

        //Handle message before Interceptor

        try {
            handle(message);
        } catch (Throwable throwable) {
            if (exceptionHandler != null) {
                exceptionHandler.accept(message, throwable);
            }
        } finally {
            message.nack(false);
        }
    }

    @Override
    public void destroy() {
        context.getDelegateFactory().releaseDelegate(delegate);
    }

    @Override
    public void handleConsumeOk(String consumerTag) {

    }

    @Override
    public void handleCancelOk(String consumerTag) {

    }

    @Override
    public void handleCancel(String consumerTag) throws IOException {

    }

    @Override
    public void handleShutdownSignal(String consumerTag, ShutdownSignalException sig) {

    }

    @Override
    public boolean autoAck() {
        return false;
    }

    @Override
    public void handleRecoverOk(String consumerTag) {

    }


    public abstract String getQueueName();


    @Override
    public Map<String, String> setHeaderEntry(String key, String value) {
        return null;
    }

    @Override
    public Set<String> getEnabledHeaderKeys() {
        return null;
    }

    @Override
    public Map<String, String> getHeaders() {
        return null;
    }

    public java.util.function.BiConsumer<Message, Throwable> getExceptionHandler() {
        return exceptionHandler;
    }

    public BaseConsumer setExceptionHandler(java.util.function.BiConsumer<Message, Throwable> exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
        return this;
    }

    public void addMessageConsumerExtractor(MessageConsumerExtractor extractor) {
        extractors.add(extractor);
    }


    @Override
    public Integer getPreFetchSize() {
        return null;
    }

    public RabbitmqDelegate getDelegate() {
        return delegate;
    }

    public BaseConsumer setDelegate(RabbitmqDelegate delegate) {
        this.delegate = delegate;
        return this;
    }
}
