package com.muppet.rabbitfriend.core;

import com.muppet.util.GsonTransient;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.AMQP.BasicProperties;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;

/**
 * Created by yuhaiqiang on 2018/6/28.
 *
 * @description
 */
public abstract class Message implements HeadersConfigurable<Object> {


    private String id;

    private String routingkey;

    @GsonTransient
    private BasicProperties basicProperties;

    @GsonTransient
    private boolean persistent = true;

    @GsonTransient
    private Integer priority = 0;

    @GsonTransient
    private Map<String, Object> headers = new HashMap<>();


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
        return null;
    }

    @Override
    public Set<String> getEnabledHeaderKeys() {
        return null;
    }

    @Override
    public Map<String, Object> getHeaders() {
        return null;
    }


    public <T> T cast() {
        return (T) this;
    }
}
