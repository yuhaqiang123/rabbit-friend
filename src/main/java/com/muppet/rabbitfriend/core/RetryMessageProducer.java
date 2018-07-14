package com.muppet.rabbitfriend.core;

import com.muppet.util.AspectAddPropertyUtil;
import com.rabbitmq.client.AMQP;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Date;
import java.util.Map;

/**
 * Created by yuhaiqiang on 2018/7/13.
 *
 * @description
 */
public class RetryMessageProducer extends BaseProducer implements MessageProducerExtractor {


    private BaseExchange exchange;

    private String DEFAULT_DEAD_LETTER_EXCHANGE;
    private String DEFAULT_DEAD_LETTER_QUEUE;

    private BaseExchange deadExchange;

    private Logger logger = LogManager.getLogger(this.getClass());

    public RetryMessageProducer(RabbitContext context) {
        super(context);
    }

    @Override
    public void start() {
        super.start();
        DEFAULT_DEAD_LETTER_EXCHANGE = exchange.getName() + Constants.DEAD_LETTER_EXCHANGE_SUFFIX;
        DEFAULT_DEAD_LETTER_QUEUE = exchange.getName() + Constants.DEAD_LETTER_QUEUE_SUFFIX;
        deadExchange = new BaseExchange(DEFAULT_DEAD_LETTER_EXCHANGE, ExchangeType.topic);
        context.declareExchange(deadExchange);

        BaseQueue queue = new BaseQueue(DEFAULT_DEAD_LETTER_QUEUE);
        queue.setHeaderEntry("x-dead-letter-exchange", exchange.getName());
        queue = context.declareQueue(queue);
        context.bind(deadExchange, queue, new RoutingKey("*"));
    }

    public void send(RetriableMessage message) {
        evalateBasicProperties(message);
        delegate.safeSend((Message) message, getExchange());
    }

    @Override
    public void extracte(Message message) {
        if (message instanceof RetriableMessage) {
            evalateBasicProperties(message.cast());
        }
    }

    private void evalateBasicProperties(RetriableMessage message) {
        String messageId = message.getId();
        if (messageId == null) {
            messageId = uuidGenerate.getUuid();
        }
        AMQP.BasicProperties properties = message.getBasicProperties();
        if (properties == null) {
            properties = new AMQP.BasicProperties().builder().messageId(messageId)
                    .deliveryMode(message.isPersistent() == true ? 2 : 1)
                    .priority(message.getPriority())
                    .timestamp(new Date(System.currentTimeMillis()))
                    .build();
        }
        Map<String, Object> headers = message.getHeaders();
        if (headers == null) {
            headers = new java.util.HashMap<>();
        }
        headers.put(message.TIMEOUT_KEY, message.getTimeout().toString());
        headers.put(Constants.RETRY_EXCHANGE_NAME, getDeadExchange().getName());
        headers.put(Constants.HEADER_EXCHANGE_NAME, getExchange().getName());
        headers.put(message.RETRY_TIMES_KEY, "0");
        headers.put(message.MAX_RETRY_TIMES, message.getMaxRetryTimes().toString());

        headers.put(message.RETRY_INTERVAL_TIME, message.getRetryInterval().toString());

        properties.setExpiration((message.getRetryInterval().toString()));
        properties.setHeaders(headers);

        AspectAddPropertyUtil.addGetBasicPropertiesAspect(message, properties);
        AspectAddPropertyUtil.addGetTimeoutAspect(message, message.getTimeout());
    }

    @Override
    public BaseExchange getExchange() {
        return exchange;
    }

    public RetryMessageProducer setExchange(BaseExchange exchange) {
        this.exchange = exchange;
        return this;
    }

    public BaseExchange getDeadExchange() {
        return deadExchange;
    }

    public RetryMessageProducer setDeadExchange(BaseExchange deadExchange) {
        this.deadExchange = deadExchange;
        return this;
    }
}
