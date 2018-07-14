package com.muppet.rabbitfriend.core;

/**
 * Created by yuhaiqiang on 2018/6/28.
 *
 * @description
 */

/**
 * 可重试消息除了添加可重试次数，也需要添加超时时间
 */
public interface RetriableMessage extends TimeoutMessage, MessageInterface, HeadersConfigurable<Object> {

    public static final String RETRY_TIMES_KEY = DEFAULT_PREFIX + "curr.retry.times";

    public static final String MAX_RETRY_TIMES = DEFAULT_PREFIX + "max.retry.times";

    public static final String RETRY_INTERVAL_TIME = DEFAULT_PREFIX + "retry.interval.time";


    public Integer getMaxRetryTimes();

    default Integer getCurrentRetryTimes() {
        return currentRetryTimes;
    }

    public Integer getRetryInterval();
}
