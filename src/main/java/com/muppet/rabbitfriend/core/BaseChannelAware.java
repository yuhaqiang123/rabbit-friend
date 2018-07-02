package com.muppet.rabbitfriend.core;

import com.rabbitmq.client.Channel;

/**
 * Created by yuhaiqiang on 2018/6/27.
 *
 * @description
 */

/**
 * 可以结合Spring 进一步的注入Channel
 */
public interface BaseChannelAware {
    public Channel getChannel();
}
