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


    /**
     * rabbitmq Server重启后这个队列是否被重建，如果设置为true,这个队列会被持久化到磁盘上
     * 但是里边的消息是否被持久化到磁盘上要看具体的消息的持久化设置
     */
    private Boolean durable = true;


    /**
     * 排他的队列
     * 1.只对首次声明它的连接（Connection）可见
     * 2.会在其连接断开的时候自动删除
     */
    private Boolean exclusize = false;


    /**
     * 在该队列最后一个消费者断开连接的时候自动被删除
     */
    private Boolean autoDelete = false;

    private Map<String, Object> arguments;


    public BaseQueue() {

    }

    public BaseQueue(String name) {
        this.setName(name);
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

    @Override
    public Map setHeaderEntry(String key, Object value) {
        arguments.put(key, value);
        return arguments;
    }
}
