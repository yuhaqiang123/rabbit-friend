package com.muppet.rabbitfriend.core;

import com.rabbitmq.client.AMQP;

/**
 * Created by yuhaiqiang on 2018/7/4.
 *
 * @description
 */
public interface MessageConvert {
    public Message loads(String s, com.rabbitmq.client.Envelope envelope, AMQP.BasicProperties basicProperties, byte[] bytes);

    public byte[] dump(Message message);
}
