package com.muppet.rabbitfriend.core;

import com.muppet.util.AspectAddPropertyUtil;
import com.muppet.util.ExceptionDSL;
import com.muppet.util.GsonUtil;
import org.apache.commons.lang.reflect.FieldUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.Objects;

/**
 * Created by yuhaiqiang on 2018/7/14.
 *
 * @description
 */
public abstract class RetryMessageConsumer extends BaseConsumer implements MessageConsumerExtractor {

    private String queueName;


    private Logger logger = LogManager.getLogger(this.getClass());


    public RetryMessageConsumer(RabbitContext context) {
        super(context);
    }


    @Override
    public void start() {
        super.start();
        this.addMessageConsumerExtractor(this);
    }


    @Override
    protected void processMessage(Message message) {
        if (!(message instanceof RetriableMessage)) {
            return;
        }
        String exchangeName = message.getBasicProperties().getHeaders().get(Constants.RETRY_EXCHANGE_NAME).toString();

        Map<String, Object> headers = message.getBasicProperties().getHeaders();
        Integer retryTimes = Integer.valueOf(headers.get(((RetriableMessage) message).RETRY_TIMES_KEY).toString());
        AspectAddPropertyUtil.addGetTimeoutAspect(message.cast(), Long.valueOf(headers.get(((RetriableMessage) message).TIMEOUT_KEY).toString()));
        AspectAddPropertyUtil.addGetCurrentRetryTimes(message.cast(), retryTimes);
        AspectAddPropertyUtil.addGetMaxRetryTimesRetry(message.cast(), Integer.valueOf(headers.get(((RetriableMessage) message).MAX_RETRY_TIMES).toString()));
        AspectAddPropertyUtil.addGetRetryInterval(message.cast(), Integer.valueOf(headers.get(((RetriableMessage) message).RETRY_INTERVAL_TIME).toString()));


        Runnable retryFunction = () -> {
            headers.put(((RetriableMessage) message).RETRY_TIMES_KEY, String.valueOf(retryTimes + 1));
            delegate.safeSend(message, new BaseExchange(exchangeName));
        };
        ExceptionDSL.throwable(() -> FieldUtils.writeField(message, "retryFunction", retryFunction, true), null, (e) -> logger.info(e));
    }

    @Override
    public void extracte(Message message) {
        processMessage(message);
    }
}
