package com.muppet.rabbitfriend.core;

import java.util.List;

/**
 * Created by yuhaiqiang on 2018/7/3.
 *
 * @description
 */
public abstract class BaseProducer implements Producer {

    protected UuidGenerate uuidGenerate;

    protected RabbitContext context;

    protected RabbitmqDelegate delegate;


    public BaseProducer(RabbitContext context) {
        this.context = context;
    }


    @Override
    public void start() {
        uuidGenerate = context.getConfiguration().getUuidGenerator();
        delegate = context.getDelegateFactory().acquireDelegate();
    }


    abstract class Envelope {

        void start() {
        }

        void count(Message msg) {
        }

        abstract void ack(MessageReply reply);

        abstract void timeout();

        abstract List<Message> getRequests();
    }

    @Override
    public BaseExchange getExchange() {
        return null;
    }

    @Override
    public RoutingKey getDefaultRoutingkey() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }


    @Override
    public void destroy() {

    }
}
