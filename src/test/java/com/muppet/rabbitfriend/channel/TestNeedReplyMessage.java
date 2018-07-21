package com.muppet.rabbitfriend.channel;

import com.muppet.rabbitfriend.core.AsyncMessageReplyCallback;
import com.muppet.rabbitfriend.core.BaseExchange;
import com.muppet.rabbitfriend.core.BaseQueue;
import com.muppet.rabbitfriend.core.ConsumerCompositor;
import com.muppet.rabbitfriend.core.ExchangeType;
import com.muppet.rabbitfriend.core.Message;
import com.muppet.rabbitfriend.core.MessageReply;
import com.muppet.rabbitfriend.core.NeedReplyMessage;
import com.muppet.rabbitfriend.core.ProducerCompositor;
import com.muppet.rabbitfriend.core.RoutingKey;
import org.junit.Test;
import org.omg.CORBA.BAD_CONTEXT;

/**
 * Created by yuhaiqiang on 2018/7/20.
 *
 * @description
 */
public class TestNeedReplyMessage extends BaseTest {

    @Test
    public void testProducer() {
        ProducerCompositor producerCompositor = context.createProducer(exchange);
        producerCompositor.start();

        BaseNeedReplyMessage base = new BaseNeedReplyMessage();
        base.setRoutingkey("TestQueue");
        base.a = new A();
        producerCompositor.send(base, new AsyncMessageReplyCallback(null) {
            @Override
            public void run(MessageReply r) {
                logger.debug(gson.toJson(r));
                stop();
            }
        });
        waitStop();
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
                BaseNeedReplyMessage baseNeedReplyMessage = message.cast();
                logger.debug(gson.toJson(baseNeedReplyMessage));
                baseNeedReplyMessage.reply(new BaseMessageReply());
                stop();
            }
        });
        waitStop();

    }

    class A {
        public String res = "RESULT";
    }

    class BaseNeedReplyMessage extends NeedReplyMessage {
        public A a;
    }

    class BaseMessageReply extends MessageReply {
        public String reply = "reply";
    }

}
