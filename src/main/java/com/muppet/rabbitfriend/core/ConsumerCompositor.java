package com.muppet.rabbitfriend.core;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by yuhaiqiang on 2018/7/14.
 *
 * @description
 */
public abstract class ConsumerCompositor extends BaseConsumer implements Consume {
    private List<BaseConsumer> consumers = new ArrayList<>();


    public ConsumerCompositor(RabbitContext context) {
        super(context);
    }

    @Override
    public BaseQueue getConsumedQueue() {
        return null;
    }


    @Override
    public void start() {
        RpcConsumer rpcConsumer = new RpcConsumer(context) {
            @Override
            protected String getQueueName() {
                return ConsumerCompositor.this.getQueueName();
            }

            @Override
            public void handle(Message message) {
                ConsumerCompositor.this.handle(message);
            }
        };

        RetryMessageConsumer retryMessageConsumer = new RetryMessageConsumer(context) {
            @Override
            protected String getQueueName() {
                return ConsumerCompositor.this.getQueueName();
            }

            @Override
            public void handle(Message message) {
                ConsumerCompositor.this.handle(message);
            }
        };
        rpcConsumer.start();
        retryMessageConsumer.start();
        consumers.add(retryMessageConsumer);
        consumers.add(rpcConsumer);
        this.addMessageConsumerExtractor(retryMessageConsumer);
        this.addMessageConsumerExtractor(rpcConsumer);
    }


    @Override
    public void destroy() {

    }

    @Override
    public Map<String, String> setHeaderEntry(String key, String value) {
        return null;
    }

    @Override
    public Set<String> getEnabledHeaderKeys() {
        return null;
    }

    @Override
    public Map<String, String> getHeaders() {
        return null;
    }
}
