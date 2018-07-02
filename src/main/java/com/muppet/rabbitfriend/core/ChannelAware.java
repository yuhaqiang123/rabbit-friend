package com.muppet.rabbitfriend.core;

import com.rabbitmq.client.Channel;

/**
 * Created by yuhaiqiang on 2018/6/27.
 *
 * @description
 */
public interface ChannelAware {
    public Channel getChannel();
}
