package com.muppet.rabbitfriend.core;

import com.rabbitmq.client.Channel;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by yuhaiqiang on 2018/6/27.
 *
 * @description
 */
public class BaseExchange implements Exchange {
    private String name;

    private ExchangeType type = ExchangeType.topic;

    private Channel channel;

    private Boolean durable = false;

    private Boolean autoDelete = false;

    private Map<String, Object> arguments = new java.util.HashMap<>();

    public BaseExchange(String name, ExchangeType type) {
        this.name = name;
        this.type = type;
    }

    public BaseExchange(String name) {
        this(name, ExchangeType.topic);
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }


    @Override
    public Map<String, Object> getHeaders() {
        return arguments;
    }

    @Override
    public Set<String> getEnabledHeaderKeys() {
        return new HashSet<String>() {
            {
            }
        };
    }

    public String getName() {
        return name;
    }

    public BaseExchange setName(String name) {
        this.name = name;
        return this;
    }

    public ExchangeType getType() {
        return type;
    }

    public BaseExchange setType(ExchangeType type) {
        this.type = type;
        return this;
    }

    public Channel getChannel() {
        return channel;
    }

    public Boolean getDurable() {
        return durable;
    }

    public BaseExchange setDurable(Boolean durable) {
        this.durable = durable;
        return this;
    }

    public Boolean getAutoDelete() {
        return autoDelete;
    }

    public BaseExchange setAutoDelete(Boolean autoDelete) {
        this.autoDelete = autoDelete;
        return this;
    }

    @Override
    public Map<String, Object> setHeaderEntry(String key, Object value) {
        arguments.put(key, value);
        return this.arguments;
    }


//返回Bindings

}
