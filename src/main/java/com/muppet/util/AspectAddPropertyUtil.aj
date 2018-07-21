package com.muppet.util;

import com.muppet.rabbitfriend.core.Message;
import com.muppet.rabbitfriend.core.MessageInterface;
import com.muppet.rabbitfriend.core.RetriableMessage;
import com.muppet.rabbitfriend.core.TimeoutMessage;
import com.rabbitmq.client.AMQP;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Created by yuhaiqiang on 2018/7/14.
 * @description
 */
public aspect AspectAddPropertyUtil {

    private Logger logger = LogManager.getLogger(this.getClass());

    public AMQP.BasicProperties MessageInterface.properties;

    public static void addGetBasicPropertiesAspect(Message message, AMQP.BasicProperties properties) {
        message.properties = properties;
    }

    AMQP.BasicProperties around (com.muppet.rabbitfriend.core.Message message):target(message) && execution(AMQP.BasicProperties com.muppet.rabbitfriend.core.Message+.getBasicProperties()){
        if (message.properties != null) {
            return message.properties;
        }
        return proceed(message);
    }

    public Long TimeoutMessage.timeoutValue;

    public static void addGetTimeoutAspect(TimeoutMessage message, Long timeout) {
        message.timeoutValue = timeout;
    }

    public transient Consumer<Integer> RetriableMessage.retryFunction;

    public static void addRetryAspect(RetriableMessage message, Consumer<Integer> retryFunction) {
        message.retryFunction = retryFunction;
    }

    /*void around(com.muppet.rabbitfriend.core.RetriableMessage message):target(message) && execution(void com.muppet.rabbitfriend.core.RetriableMessage+.retry(..)){
        message.retryFunction.run();
    }*/

    public Integer RetriableMessage.maxRetryTimes;

    public static void addGetMaxRetryTimesRetry(RetriableMessage message, Integer maxRetryTimes) {
        message.maxRetryTimes = maxRetryTimes;
    }

    Integer around(com.muppet.rabbitfriend.core.RetriableMessage message):target(message) && execution(Integer com.muppet.rabbitfriend.RetriableMessage+.getMaxRetryTimes(..)){
        return message.maxRetryTimes;
    }


    public Integer RetriableMessage.currentRetryTimes;

    public static void addGetCurrentRetryTimes(RetriableMessage message, Integer currentRetryTimes) {
        message.currentRetryTimes = currentRetryTimes;
    }

    Integer around(com.muppet.rabbitfriend.core.RetriableMessage message):target(message) && execution(Integer com.muppet.rabbitfriend.RetriableMessage+.getCurrentRetryTimes(..)){
        return message.currentRetryTimes;
    }

    public Integer RetriableMessage.retryInterval;


    public static void addGetRetryInterval(RetriableMessage message, Integer maxRetryTimes) {
        message.retryInterval = maxRetryTimes;
    }

    Integer around(com.muppet.rabbitfriend.core.RetriableMessage message):target(message) && execution(Integer com.muppet.rabbitfriend.RetriableMessage+.getRetryInterval(..)){
        return message.retryInterval;
    }
}
