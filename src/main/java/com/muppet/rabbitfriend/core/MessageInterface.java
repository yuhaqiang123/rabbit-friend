package com.muppet.rabbitfriend.core;

import com.rabbitmq.client.AMQP;

/**
 * Created by yuhaiqiang on 2018/7/13.
 *
 * @description
 */
public interface MessageInterface {
    public String getId();

    public String getRoutingkey();

    public AMQP.BasicProperties getBasicProperties();

    public boolean isPersistent();

    public Integer getPriority();

    public <T> T cast();

    public void ack();

    public void nack(Boolean requeue);

    public void nack();

    public void retry();
}
