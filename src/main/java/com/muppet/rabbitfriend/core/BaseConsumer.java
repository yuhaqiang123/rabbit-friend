package com.muppet.rabbitfriend.core;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.muppet.util.ExceptionDSL;
import com.muppet.util.GsonTransient;
import com.muppet.util.GsonTypeCoder;
import com.muppet.util.GsonUtil;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;
import okio.Timeout;
import org.apache.commons.lang.reflect.FieldUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.reflections.ReflectionUtils;
import org.reflections.Reflections;

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created by yuhaiqiang on 2018/7/4.
 *
 * @description
 */
public abstract class BaseConsumer implements Consumer, Lifecycle, Consume, Consume.AutoAckEnable, RabbitFriendComponent {

    private RabbitContext context;

    private Logger logger = LogManager.getLogger(this.getClass());

    private HashMap<String, RpcProducer> rpcProducers = new HashMap<>();

    private Gson gson = GsonUtil.getGson();

    protected Channel channel;

    private UuidGenerate uuidGenerate;

    private RabbitmqDelegate delegate;

    private RpcProducer rpcProducer;

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


    @Override
    public void start() {
        delegate = context.getDelegateFactory().acquireDelegate();
        uuidGenerate = context.getConfiguration().getUuidGenerator();
    }

    @Override
    public void destroy() {

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

    protected boolean timeoutCheck(Message message) {
        if (message instanceof TimeoutMessage) {
            TimeoutMessage timeoutMessage = (TimeoutMessage) message;
            timeoutMessage.getTimeout();
            Date createDate = message.getBasicProperties().getTimestamp();
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
        boolean isTimeout = timeoutCheck(message);
        if (isTimeout) {
            //TODO reply the error MessageReply
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

        if (message instanceof NeedReplyMessage) {
            String exchangeName = properties.getHeaders().get(Producer.HEADER_EXCHANGE_NAME).toString();


            Function<MessageReply, Void> replyFunc = ((reply) -> {
                reply((NeedReplyMessage) message, reply, new BaseExchange(exchangeName, ExchangeType.topic));
                return null;
            });
            ExceptionDSL.throwable(() -> FieldUtils.writeField(message, "replyFunc", replyFunc, true));
        }

        //Handle message before Interceptor
        handle(message);
        message.ack();
    }

    public void reply(NeedReplyMessage message, MessageReply reply, BaseExchange exchange) {
        reply.setRequestMessage(message);
        evalateBasicProperties(reply);
        delegate.safeSend(reply, exchange);
    }

    private void evalateBasicProperties(MessageReply reply) {
        NeedReplyMessage message = reply.getRequestMessage();
        AMQP.BasicProperties properties = message.getBasicProperties();
        String routingKey = properties.getReplyTo();
        if (routingKey == null) {
            routingKey = message.getReplyTo();
        }
        if (routingKey == null) {
            throw new RabbitFriendException("routing key can not be null");
        }
        reply.setRoutingkey(routingKey);
        String replyId = reply.getId();
        if (replyId == null) {
            replyId = uuidGenerate.getUuid();
        }
        reply.setId(replyId);

        AMQP.BasicProperties.Builder builder = new AMQP.BasicProperties.Builder();
        builder.messageId(replyId)
                .correlationId(message.getId())
                .deliveryMode(message.isPersistent() == true ? 2 : 1)
                .priority(message.getPriority())
                .timestamp(Calendar.getInstance().getTime())
                .replyTo(properties.getReplyTo())
                .headers(message.getHeaders());
        reply.setBasicProperties(builder.build());
    }


    public BaseQueue getConsumedQueue() {
        return new BaseQueue(getQueueName());
    }

    protected abstract String getQueueName();


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

    @Override
    public Channel getChannel() {
        return channel;
    }
}
