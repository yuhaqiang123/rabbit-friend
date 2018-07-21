package com.muppet.rabbitfriend.core;

import com.muppet.util.ExceptionDSL;
import com.rabbitmq.client.AMQP;
import org.apache.commons.lang.reflect.FieldUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Calendar;
import java.util.function.Function;

/**
 * Created by yuhaiqiang on 2018/7/9.
 *
 * @description
 */
public abstract class RpcConsumer extends BaseConsumer implements MessageConsumerExtractor {


    private Logger logger = LogManager.getLogger(this.getClass());

    private RpcMessageConsumerExtractor extractor;

    public RpcConsumer(RabbitContext context) {
        super(context);
    }


    @Override
    public void start() {
        super.start();
        initializeDelegate();
        extractor = new RpcMessageConsumerExtractor(this);
        this.addMessageConsumerExtractor(this);
    }

    public void reply(NeedReplyMessage message, MessageReply reply, BaseExchange exchange) {
        reply.setRequestMessage(message);
        evalateBasicProperties(reply);
        delegate.safeSend(reply, exchange);
    }

    public class RpcMessageConsumerExtractor implements MessageConsumerExtractor {


        private RpcConsumer consumer;

        public RpcMessageConsumerExtractor(RpcConsumer consumer) {
            this.consumer = consumer;
        }

        public void extracte(Message message) {
            AMQP.BasicProperties properties = message.getBasicProperties();
            if (message instanceof NeedReplyMessage) {
                String exchangeName = properties.getHeaders().get(Constants.HEADER_EXCHANGE_NAME).toString();

                Function<MessageReply, Void> replyFunc = ((reply) -> {
                    this.consumer.reply((NeedReplyMessage) message, reply, new BaseExchange(exchangeName));
                    return null;
                });
                ExceptionDSL.throwable(() -> FieldUtils.writeField(message, "replyFunc", replyFunc, true));
            }
        }
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

    @Override
    public void extracte(Message message) {
        extractor.extracte(message);
    }
}
