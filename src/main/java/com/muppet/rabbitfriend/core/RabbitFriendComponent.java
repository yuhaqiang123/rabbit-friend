package com.muppet.rabbitfriend.core;

import com.rabbitmq.client.Channel;

/**
 * Created by yuhaiqiang on 2018/7/7.
 *
 * @description
 */
public interface RabbitFriendComponent extends Lifecycle {
    public Channel getChannel();
}
