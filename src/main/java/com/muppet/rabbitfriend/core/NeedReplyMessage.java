package com.muppet.rabbitfriend.core;

import com.muppet.util.GsonTransient;
import com.rabbitmq.client.BasicProperties;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created by yuhaiqiang on 2018/6/28.
 *
 * @description
 */
public class NeedReplyMessage extends Message implements TimeoutMessage {


    @GsonTransient
    private transient String replyTo;

    private Long timeout = 30 * 1000L;

    @GsonTransient
    private transient Function<MessageReply, Void> replyFunc;

    public String getReplyTo() {
        return replyTo;
    }

    public NeedReplyMessage setReplyTo(String replyTo) {
        this.replyTo = replyTo;
        return this;
    }

    //@Override
    public TimeoutMessage setTimeout(Long timeout) {
        this.timeout = timeout;
        return this;
    }

    public Long getTimeout() {
        return timeout;
    }


    public void reply(MessageReply reply) {
        replyFunc.apply(reply);
    }
}
