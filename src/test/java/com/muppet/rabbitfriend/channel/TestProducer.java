package com.muppet.rabbitfriend.channel;

import com.muppet.rabbitfriend.core.AsyncSafeCallback;
import com.muppet.rabbitfriend.core.BaseConsumer;
import com.muppet.rabbitfriend.core.BaseExchange;
import com.muppet.rabbitfriend.core.BaseQueue;
import com.muppet.rabbitfriend.core.ExchangeType;
import com.muppet.rabbitfriend.core.Message;
import com.muppet.rabbitfriend.core.MessageReply;
import com.muppet.rabbitfriend.core.NeedReplyMessage;
import com.muppet.rabbitfriend.core.RabbitConfiguration;
import com.muppet.rabbitfriend.core.RabbitContext;
import com.muppet.rabbitfriend.core.RabbitFriendUtilExtension;
import com.muppet.rabbitfriend.core.RoutingKey;
import com.muppet.rabbitfriend.core.RpcProducer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import static com.muppet.util.GsonUtil.*;

import java.util.concurrent.CountDownLatch;

/**
 * Created by yuhaiqiang on 2018/6/28.
 *
 * @description
 */
public class TestProducer {


    Logger logger = LogManager.getLogger(this.getClass());


    @Test
    public void test2() {

    }

    @Test
    public void test1() {

        CountDownLatch latch = new CountDownLatch(1);
        RabbitConfiguration configuration = new RabbitConfiguration();
        RabbitFriendUtilExtension extension = new RabbitFriendUtilExtension();
        configuration.setUuidGenerator(extension);
        configuration.setUsername("muppet");
        configuration.setPassword("muppet");
        configuration.setIps(new String[]{"127.0.0.1"});
        configuration.setChannelPoolSize(20);
        configuration.setDefaultReplyToQueue("tt");
        RabbitContext context = configuration.getRabbitContext();
        context.start();

        BaseExchange exchange = context.declareExchangeIfabsent("b", ExchangeType.topic);
        BaseQueue queue = context.declareQueueIfAbsent("vv");
        context.bind(exchange, queue, new RoutingKey("yy"));
        RpcProducer producer = new RpcProducer(context);
        producer.setExchange(exchange);
        producer.start();

        NeedReplyMessage message = new NeedReplyMessage();
        message.setTimeout(5000L);
        message.setRoutingkey("yy");
        context.registerConsumer(new BaseConsumer(context) {

            @Override
            public void handle(Message message) {
                logger.info("成功收到消息" + message.getId());
                message.ack();
                if (message instanceof NeedReplyMessage) {
                    NeedReplyMessage m = message.cast();
                    m.reply(new AMessageReply());
                }
            }

            public String getQueueName() {
                return "vv";
            }

        });
        class AMessageReply extends MessageReply {
            private String result = "于海强";

            {
                success = true;
            }
        }
        message.setId("1");
        logger.info("成功发送消息{}", message.getId());
        producer.send(message, new AsyncSafeCallback(null) {
            @Override
            public void run(MessageReply r) {
                logger.info("成功收到返回值, {}", r.getSuccess());
                latch.countDown();
            }
        });

        try {
            latch.await();
        } catch (Exception e) {

        }
    }

    class AMessageReply extends MessageReply {
        private String result = "于海强";

        {
            success = true;
        }
    }
}
