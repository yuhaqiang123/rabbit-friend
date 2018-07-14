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
import com.muppet.rabbitfriend.core.RabbitConfiguration;
import com.muppet.rabbitfriend.core.RabbitContext;
import com.muppet.rabbitfriend.core.RabbitFriendUtilExtension;
import com.muppet.rabbitfriend.core.RetriableMessage;
import com.muppet.rabbitfriend.core.RetryMessageConsumer;
import com.muppet.rabbitfriend.core.RetryMessageProducer;
import com.muppet.rabbitfriend.core.RoutingKey;
import com.muppet.rabbitfriend.core.TimeoutMessage;
import com.muppet.util.GsonUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import java.util.Scanner;
import java.util.concurrent.CountDownLatch;

/**
 * Created by yuhaiqiang on 2018/7/13.
 *
 * @description
 */
public class TestRetryMessageTemplate {


    private Logger logger = LogManager.getLogger(this.getClass());

    @Test
    public void test1() {
        RabbitConfiguration configuration = new RabbitConfiguration();
        RabbitFriendUtilExtension extension = new RabbitFriendUtilExtension();
        configuration.setUuidGenerator(extension);
        configuration.setUsername("muppet");
        configuration.setPassword("muppet");
        configuration.setIps(new String[]{"127.0.0.1"});
        configuration.setChannelPoolSize(20);
        configuration.setDefaultReplyToQueue("tt");

        /***
         * 通过context 注册Producer,Consumer
         */
        RabbitContext context = configuration.getRabbitContext();
        context.start();
        CountDownLatch latch = new CountDownLatch(1);

        ProducerCompositor producer = new ProducerCompositor(context);
        BaseExchange exchange = new BaseExchange("b", ExchangeType.topic);
        producer.setExchange(exchange);
        exchange = context.declareExchangeIfabsent("b", ExchangeType.topic);
        BaseQueue queue = context.declareQueueIfAbsent("vv");
        context.bind(exchange, queue, new RoutingKey("yy"));
        producer.start();

        ARetrableMessage message = new ARetrableMessage();
        message.setRoutingkey("yy");
        producer.send(message, new AsyncMessageReplyCallback(null) {
            @Override
            public void run(MessageReply r) {
                logger.info(GsonUtil.toDefaultJson(r));
                latch.countDown();
            }
        });
        try {
            latch.await();
        } catch (Exception e) {

        }
    }

    @Test
    public void test2() {
        RabbitConfiguration configuration = new RabbitConfiguration();
        RabbitFriendUtilExtension extension = new RabbitFriendUtilExtension();
        configuration.setUuidGenerator(extension);
        configuration.setUsername("muppet");
        configuration.setPassword("muppet");
        configuration.setIps(new String[]{"127.0.0.1"});
        configuration.setChannelPoolSize(20);
        configuration.setDefaultReplyToQueue("tt");

        /***
         * 通过context 注册Producer,Consumer
         */
        RabbitContext context = configuration.getRabbitContext();
        context.start();


        ConsumerCompositor consumerCompositor = new ConsumerCompositor(context) {
            @Override
            protected String getQueueName() {
                return "vv";
            }

            @Override
            public void handle(Message message) {
                logger.info(GsonUtil.toDefaultJson(message));
                AMessageReply messageReply = new AMessageReply();
                NeedReplyMessage needReplyMessage = message.cast();
                try {
                    if (message instanceof RetriableMessage) {
                        RetriableMessage retriableMessage = message.cast();
                        if (retriableMessage.getCurrentRetryTimes() < retriableMessage.getMaxRetryTimes()) {
                            retriableMessage.retry();
                        } else {
                            needReplyMessage.reply(messageReply);
                        }
                        return;
                    }
                    needReplyMessage.reply(messageReply);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        context.registerConsumer(consumerCompositor);
        new Scanner(System.in).nextLine();
    }

}

class ARetrableMessage extends NeedReplyMessage implements RetriableMessage {

    //@Override
    public TimeoutMessage setTimeout(Long timeout) {
        return this;
    }

    private String name = "于海强";

    @Override
    public Long getTimeout() {
        return 100000L;
    }

    @Override
    public Integer getMaxRetryTimes() {
        return 3;
    }

    @Override
    public Integer getRetryInterval() {
        return 10000;
    }
}

class AMessageReply extends MessageReply {
    private String result = "成功";
}