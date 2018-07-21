package com.muppet.rabbitfriend.core;

import com.muppet.util.AspectAddPropertyUtil;
import com.muppet.util.ExceptionDSL;
import org.apache.commons.lang.reflect.FieldUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Created by yuhaiqiang on 2018/7/14.
 *
 * @description
 */
public abstract class RetryMessageConsumer extends BaseConsumer implements MessageConsumerExtractor {

    private String queueName;


    private RetryMessageConsumerExtractor extractor;

    private Logger logger = LogManager.getLogger(this.getClass());


    public RetryMessageConsumer(RabbitContext context) {
        super(context);
    }


    @Override
    public void start() {
        super.start();
        initializeDelegate();
        extractor = new RetryMessageConsumerExtractor(delegate);
        this.addMessageConsumerExtractor(this);
    }


    @Override
    public void extracte(Message message) {
        extractor.extracte(message);
    }
}


class RetryMessageConsumerExtractor implements MessageConsumerExtractor {


    private RabbitmqDelegate delegate;

    Logger logger = LogManager.getLogger(this.getClass());

    public RetryMessageConsumerExtractor(RabbitmqDelegate delegate) {
        this.delegate = delegate;
    }


    @Override
    public void extracte(Message message) {
        if (!(message instanceof RetriableMessage)) {
            return;
        }
        String exchangeName = message.getBasicProperties().getHeaders().get(Constants.RETRY_EXCHANGE_NAME).toString();

        Map<String, Object> headers = message.getBasicProperties().getHeaders();
        Integer retryTimes = Integer.valueOf(headers.get((Constants.HEADER_RETRY_TIMES_KEY)).toString());

        AspectAddPropertyUtil.addGetCurrentRetryTimes(message.cast(), retryTimes);
        AspectAddPropertyUtil.addGetMaxRetryTimesRetry(message.cast(), Integer.valueOf(headers.get(Constants.HEADER_MAX_RETRY_TIMES).toString()));
        AspectAddPropertyUtil.addGetRetryInterval(message.cast(), Integer.valueOf(headers.get(Constants.HEADER_RETRY_INTERVAL_TIME).toString()));


        Consumer<Integer> retryFunction = (delay) -> {
            headers.put(Constants.HEADER_RETRY_TIMES_KEY, String.valueOf(retryTimes + 1));
            Integer finalDelay = delay;
            if (delay == null) {
                finalDelay = ((RetriableMessage) message).getRetryInterval();
            }
            message.getBasicProperties().setExpiration(String.valueOf(finalDelay));
            delegate.safeSend(message, new BaseExchange(exchangeName));
        };
        ExceptionDSL.throwable(() -> FieldUtils.writeField(message, "retryFunction", retryFunction, true), null, (e) -> logger.info(e));
    }
}