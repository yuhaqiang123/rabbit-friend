package com.muppet.rabbitfriend.core;

import java.util.concurrent.TimeUnit;

/**
 * Created by yuhaiqiang on 2018/6/28.
 * <p>
 * 超时消息需要在消息头中声明create time. 各节点之间如果时间不一致比较严重
 * Timeout消息会出现不准的情况，这个timeout并不是 消息在rabbitmq中的ttl
 * 而是整个消息的发收以及处理的时间
 *
 * @description
 */
public interface TimeoutMessage {
    public Long getTimeout();
}
