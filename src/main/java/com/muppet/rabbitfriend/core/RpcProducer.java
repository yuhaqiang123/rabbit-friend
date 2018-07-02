package com.muppet.rabbitfriend.core;

/**
 * Created by yuhaiqiang on 2018/6/27.
 *
 * @description
 */
public class RpcProducer implements Producer {

    private RabbitContext context;

    public BaseExchange getExchange() {
        return null;
    }

    public RoutingKey getRoutingKey() {
        return null;
    }

    public void RpcProducer(RabbitContext context) {
        this.context = context;
    }


    @Override
    public RoutingKey getDefaultRoutingkey() {
        return null;
    }

    public void send(NeedReplyMessage message, AsyncSafeCallback callback) {
        context.getDelegate().safeSend(message, getExchange());
        
    }

    public void send(NeedReplyMessage message) {

    }

    public void call(NeedReplyMessage message) {

    }

    @Override
    public String getName() {
        return null;
    }
}
