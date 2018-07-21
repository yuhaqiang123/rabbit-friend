package com.muppet.rabbitfriend.channel;

import com.muppet.rabbitfriend.core.ConsumerCompositor;
import com.muppet.rabbitfriend.core.Message;
import com.muppet.rabbitfriend.core.ProducerCompositor;
import com.muppet.rabbitfriend.core.RetriableMessage;
import org.junit.Test;

/**
 * Created by yuhaiqiang on 2018/7/21.
 *
 * @description
 */
public class TestRetryMessage extends BaseTest {

    @Test
    public void testProducer() {
        ProducerCompositor producerCompositor = context.createProducer(exchange);
        producerCompositor.start();

        BaseRetryMessage message = new BaseRetryMessage();
        message.setRoutingkey(routingKey.getRoutingKey());
        producerCompositor.send(message);
    }


    @Test
    public void testConsumer() {
        context.registerConsumer(new ConsumerCompositor(context) {
            @Override
            public String getQueueName() {
                return queue.getName();
            }

            @Override
            public void handle(Message message) {
                BaseRetryMessage baseRetryMessage = message.cast();
                logger.debug("current retry times[{}]", baseRetryMessage.getCurrentRetryTimes());
                if (baseRetryMessage.getCurrentRetryTimes() < baseRetryMessage.getMaxRetryTimes()) {
                    logger.debug(" retry message[{}]", gson.toJson(baseRetryMessage));
                    baseRetryMessage.retry();
                } else {
                    logger.debug("succeed to handle this message");
                    stop();
                }
            }
        });
        waitStop();
    }

}

class BaseRetryMessage extends Message implements RetriableMessage {

    private String name = "Base retry message";

    @Override
    public Integer getMaxRetryTimes() {
        return 4;
    }

    @Override
    public Integer getRetryInterval() {
        return 5000;
    }
}
