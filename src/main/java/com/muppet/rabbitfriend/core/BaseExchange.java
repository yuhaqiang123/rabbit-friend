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

    private ExchangeType type;

    private Channel channel;

    private Boolean durable = false;

    private Boolean autoDelete = false;

    private Map<String, Object> arguments;

    public BaseExchange(String name, ExchangeType type) {
        this.name = name;
        this.type = type;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }


    @Override
    public Map<String, String> getHeaders() {
        return null;
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
    public Map setHeaderEntry(String key, Object value) {
        return null;
    }


//返回Bindings

}
