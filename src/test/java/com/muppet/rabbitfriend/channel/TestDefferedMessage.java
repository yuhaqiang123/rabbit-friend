package com.muppet.rabbitfriend.channel;

import com.muppet.rabbitfriend.core.ConsumerCompositor;
import com.muppet.rabbitfriend.core.DefferedMessage;
import com.muppet.rabbitfriend.core.Message;
import com.muppet.rabbitfriend.core.ProducerCompositor;
import org.junit.Test;

/**
 * Created by yuhaiqiang on 2018/7/21.
 *
 * @description
 */
public class TestDefferedMessage extends BaseTest {

    @Test
    public void testProducer() {
        ProducerCompositor producerCompositor = context.createProducer(exchange);
        producerCompositor.start();

        for (int i = 0; i < 100; i++) {
            BaseDefferedMessage message = new BaseDefferedMessage();
            message.setRoutingkey(routingKey.getRoutingKey());
            producerCompositor.send(message);
        }
    }

    @Test
    public void testConsumer() {
        context.registerConsuimerCompositor(new ConsumerCompositor(context) {
            @Override
            public String getQueueName() {
                return queue.getName();
            }

            @Override
            public void handle(Message message) {
                logger.debug("time interval:{}", (System.currentTimeMillis() - message.getBasicProperties().getTimestamp().getTime()) / 1000);
                //stop();
            }
        }, 20);
        waitStop();
    }

}

class BaseDefferedMessage extends Message implements DefferedMessage {

    private String name = "延迟消息";

    @Override
    public Integer getDefferedTime() {
        return 10000;
    }
}
