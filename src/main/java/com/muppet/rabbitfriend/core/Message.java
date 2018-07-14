package com.muppet.rabbitfriend.core;

import com.muppet.util.GsonTransient;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.AMQP.BasicProperties;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created by yuhaiqiang on 2018/6/28.
 *
 * @description
 */
public abstract class Message implements HeadersConfigurable<Object>, MessageInterface {


    protected String id;

    protected String routingkey;

    @GsonTransient
    protected transient AMQP.BasicProperties basicProperties;

    @GsonTransient
    protected transient boolean persistent = true;

    @GsonTransient
    protected transient Integer priority = 0;

    protected transient BiFunction<Boolean, Boolean, Void> ackFunc;

    @GsonTransient
    protected transient Map<String, Object> headers = new HashMap<>();


    public String getId() {
        return id;
    }

    public Message setId(String id) {
        this.id = id;
        return this;
    }

    public String getRoutingkey() {
        return routingkey;
    }

    public Message setRoutingkey(String routingkey) {
        this.routingkey = routingkey;
        return this;
    }

    public AMQP.BasicProperties getBasicProperties() {
        return basicProperties;
    }

    public Message setBasicProperties(AMQP.BasicProperties basicProperties) {
        this.basicProperties = basicProperties;
        return this;
    }

    public boolean isPersistent() {
        return persistent;
    }

    public Message setPersistent(boolean persistent) {
        this.persistent = persistent;
        return this;
    }

    public Integer getPriority() {
        return priority;
    }

    public Message setPriority(Integer priority) {
        this.priority = priority;
        return this;
    }

    @Override
    public Map<String, Object> setHeaderEntry(String key, Object value) {
        headers.put(key, value);
        return headers;
    }

    @Override
    public Set<String> getEnabledHeaderKeys() {
        return headers.keySet();
    }

    @Override
    public Map<String, Object> getHeaders() {
        return headers;
    }


    public <T> T cast() {
        return (T) this;
    }


    private void ack(Boolean ack, Boolean requeue) {
        ackFunc.apply(ack, requeue);
    }

    /**
     * 确认该消息.
     * 目前所有的ack都会被默认调用，已防止代码bug,漏掉ack
     */
    public void ack() {
        ack(true, false);
    }

    /**
     * nack 该消息，一个消息只能被ack，或nack方法调用一次，其后调用均被忽略
     *
     * @param requeue 是否重新入队列，当选择入队列时需要慎重，可能会出现消费->nack->入队列->再消费的死循环.
     */
    public void nack(Boolean requeue) {
        ack(false, requeue);
    }

    public void nack() {
        ack(false, false);
    }

    private transient Runnable retryFunction;

    @Override
    public void retry() {
        if (retryFunction == null) {
            throw new RabbitFriendException("unsupported operation, please implements RetriableMessage");
        }
        retryFunction.run();
    }
}
