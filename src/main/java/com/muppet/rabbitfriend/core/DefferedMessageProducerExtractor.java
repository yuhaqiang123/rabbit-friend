package com.muppet.rabbitfriend.core;

import com.muppet.util.AspectAddPropertyUtil;
import com.muppet.util.ExceptionDSL;
import com.rabbitmq.client.AMQP;
import org.apache.commons.lang.reflect.FieldUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Created by yuhaiqiang on 2018/7/20.
 *
 * @description
 */
public class DefferedMessageProducerExtractor implements MessageProducerExtractor {
    private RabbitContext context;

    private BaseExchange defferedExchange;

    private BaseQueue deadQueue;

    private Logger logger = LogManager.getLogger(this.getClass());

    public DefferedMessageProducerExtractor(RabbitContext context, BaseExchange sendExchange) {
        this.context = context;
        String deadExchangeName = sendExchange.getName() + Constants.DEAD_LETTER_EXCHANGE_SUFFIX;
        defferedExchange = new BaseExchange(deadExchangeName);
        BaseQueue queue = new BaseQueue(sendExchange.getName() + Constants.DEAD_LETTER_QUEUE_SUFFIX);
        queue.setHeaderEntry("x-dead-letter-exchange", sendExchange.getName());
        context.declareExchange(defferedExchange);
        context.declareQueue(queue);
        context.bind(defferedExchange, queue, RoutingKey.ALL_ROUTE_KEY);
    }


    @Override
    public void extracte(Message message) {
        if (!(message instanceof DefferedMessage)) {
            return;
        }
        DefferedMessage defferedMessage = message.cast();
        AMQP.BasicProperties properties = message.getBasicProperties();
        Integer defferedTime = Optional.ofNullable(((DefferedMessage) message).getDefferedTime()).orElseGet(() -> 0);

        if (properties == null) {
            properties = new AMQP.BasicProperties.Builder()
                    .expiration(String.valueOf(String.valueOf(defferedTime)))
                    .deliveryMode(message.isPersistent() == true ? 2 : 1)
                    .priority(message.getPriority())
                    .timestamp(new Date(System.currentTimeMillis()))
                    .build();
        } else {
            properties.setExpiration(String.valueOf(defferedTime));
        }
        Map<String, Object> headers = properties.getHeaders();
        if (headers == null) {
            headers = new HashMap<>();
        }
        headers.put(Constants.HEADER_DEFFERED_EXCHANGE_NAME, defferedExchange.getName());
        headers.put(Constants.HEADER_DEFFERED_MESSAGE_TIME, String.valueOf(defferedMessage.getDefferedTime()));
        AMQP.BasicProperties finalProperties = properties;
        Map<String, Object> finalHeaders = headers;
        ExceptionDSL.throwable(() -> FieldUtils.writeField(finalProperties, "headers", finalHeaders, true));
        AspectAddPropertyUtil.addGetBasicPropertiesAspect(message, finalProperties);
    }

    @Override
    public BaseExchange getSendExchange(Message message) {
        if (message instanceof DefferedMessage) {
            return defferedExchange;
        }
        return null;
    }
}
