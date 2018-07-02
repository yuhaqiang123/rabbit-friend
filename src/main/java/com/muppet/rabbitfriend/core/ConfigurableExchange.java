package com.muppet.rabbitfriend.core;

import com.rabbitmq.client.Channel;
import okhttp3.Route;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by yuhaiqiang on 2018/6/29.
 *
 * @description
 */
public class ConfigurableExchange extends BaseExchange {

    private RabbitContext context;

    private List<QueueBindTuple> bindTuples = new ArrayList<>();

    class QueueBindTuple {
        public QueueBindTuple(Queue queue, RoutingKey routingKey) {
            this.queue = queue;
            this.routingKey = routingKey;
        }

        Queue queue;
        RoutingKey routingKey;
    }

    public ConfigurableExchange(String name, ExchangeType type, RabbitContext context) {
        super(name, type);
        this.context = context;
    }

    public void bindQueue(BaseQueue queue, RoutingKey routingKey) {
        context.channelExecute(channel -> {
            try {
                channel.queueBind(queue.getName(), this.getName(), routingKey.getRoutingKey());
                bindTuples.add(new QueueBindTuple(queue, routingKey));
            } catch (IOException e) {
                throw new RabbitFriendException(e);
            }
            return null;
        });
    }


    public void bindQueue(List<BaseQueue> queues, RoutingKey routingKey) {
        for (BaseQueue queue : queues) {
            bindQueue(queue, routingKey);
        }
    }

    public BaseQueue bindQueue(String queueName, RoutingKey routingKey) {
        BaseQueue queue = new BaseQueue(queueName);
        bindQueue(queue, routingKey);
        return queue;
    }

}
