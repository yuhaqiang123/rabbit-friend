package com.muppet.util;

import com.muppet.rabbitfriend.core.MessageInterface;
import com.rabbitmq.client.AMQP;

/**
 * Created by yuhaiqiang on 2018/7/13.
 * @description
 */
public aspect AopMethodInvoker {

/*
    AMQP.BasicProperties around(com.muppet.rabbitfriend.core.MessageInterface message): target(message) && execution(void com.muppet.rabbitfriend.core.MessageInterface+.getBasicProperties(..)){
        return message.properties;
    }
*/


}
