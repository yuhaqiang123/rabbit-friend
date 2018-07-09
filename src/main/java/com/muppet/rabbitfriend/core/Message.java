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
public abstract class Message implements HeadersConfigurable<Object> {


    protected String id;

    protected String routingkey;

    @GsonTransient
    protected transient BasicProperties basicProperties;

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

    public BasicProperties getBasicProperties() {
        return basicProperties;
    }

    public Message setBasicProperties(BasicProperties basicProperties) {
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

    public void ack() {
        ack(true, false);
    }

    public void nack(Boolean requeue) {
        ack(false, requeue);
    }
}
