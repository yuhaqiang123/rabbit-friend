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

    public Integer getMaxRetryTimes();

    default Integer getCurrentRetryTimes() {
        return currentRetryTimes;
    }

    public Integer getRetryInterval();
}
