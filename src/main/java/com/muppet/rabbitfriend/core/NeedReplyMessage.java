package com.muppet.rabbitfriend.core;

import com.rabbitmq.client.BasicProperties;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Created by yuhaiqiang on 2018/6/28.
 *
 * @description
 */
public class NeedReplyMessage extends Message implements TimeoutMessage {

    protected BasicProperties properties;

    private String replyTo;

    private Long timeout;

    public String getReplyTo() {
        return replyTo;
    }

    public NeedReplyMessage setReplyTo(String replyTo) {
        this.replyTo = replyTo;
        return this;
    }

    @Override
    public TimeoutMessage setTimeout(Long timeout) {
        return null;
    }

    public Long getTimeout() {
        return null;
    }


    public BasicProperties getProperties() {
        return properties;
    }

    public NeedReplyMessage setProperties(BasicProperties properties) {
        this.properties = properties;
        return this;
    }
}
