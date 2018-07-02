package com.muppet.rabbitfriend.core;


import java.util.Map;
import java.util.Set;

/**
 * Created by yuhaiqiang on 2018/6/27.
 *
 * @description
 */
public class BaseQueue implements Queue {
    private String name;

    private Boolean durable = false;

    private Boolean exclusize = true;

    private Boolean autoDelete = true;

    private Map<String, Object> arguments;


    public BaseQueue() {

    }

    public BaseQueue(String name) {
        this.setName(name);
    }

    @Override
    public void setHeaders(String key, String value) {

    }

    @Override
    public Set<String> getEnabledHeaderKeys() {
        return null;
    }

    @Override
    public Map<String, String> getHeaders() {
        return null;
    }

    public String getName() {
        return name;
    }

    public BaseQueue setName(String name) {
        this.name = name;
        return this;
    }

    public Boolean getDurable() {
        return durable;
    }

    public BaseQueue setDurable(Boolean durable) {
        this.durable = durable;
        return this;
    }

    public Boolean getExclusize() {
        return exclusize;
    }

    public BaseQueue setExclusize(Boolean exclusize) {
        this.exclusize = exclusize;
        return this;
    }

    public Boolean getAutoDelete() {
        return autoDelete;
    }

    public BaseQueue setAutoDelete(Boolean autoDelete) {
        this.autoDelete = autoDelete;
        return this;
    }

    public Map<String, Object> getArguments() {
        return arguments;
    }

    public BaseQueue setArguments(Map<String, Object> arguments) {
        this.arguments = arguments;
        return this;
    }
}
