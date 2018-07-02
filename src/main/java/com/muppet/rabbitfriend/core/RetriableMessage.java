package com.muppet.rabbitfriend.core;

/**
 * Created by yuhaiqiang on 2018/6/28.
 *
 * @description
 */
public interface RetriableMessage extends TimeoutMessage {
    public Integer getMaxRetryTimes();

    public Integer getCurrentRetiableTimes();

    public Integer setRetriableTimes(Integer times);
}
