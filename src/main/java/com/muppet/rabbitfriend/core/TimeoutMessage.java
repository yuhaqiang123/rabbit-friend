package com.muppet.rabbitfriend.core;

import java.util.concurrent.TimeUnit;

/**
 * Created by yuhaiqiang on 2018/6/28.
 *
 * @description
 */
public interface TimeoutMessage extends Message {
    public TimeUnit getTimeUnit();

    public Long getTimeout();
}
