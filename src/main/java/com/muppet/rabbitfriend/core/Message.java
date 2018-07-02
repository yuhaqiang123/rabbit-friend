package com.muppet.rabbitfriend.core;

import com.rabbitmq.client.AMQP;

/**
 * Created by yuhaiqiang on 2018/6/28.
 *
 * @description
 */
public interface Message extends HeadersConfigurable {
    public byte[] getData();

    public String getRoutingkey();

    public AMQP.BasicProperties getBasicProperties();
}
