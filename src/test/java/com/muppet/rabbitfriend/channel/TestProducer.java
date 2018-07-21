package com.muppet.rabbitfriend.channel;

import com.muppet.rabbitfriend.core.AsyncMessageReplyCallback;
import com.muppet.rabbitfriend.core.AsyncSafeCallback;
import com.muppet.rabbitfriend.core.BaseExchange;
import com.muppet.rabbitfriend.core.BaseQueue;
import com.muppet.rabbitfriend.core.ErrorCode;
import com.muppet.rabbitfriend.core.ExchangeType;
import com.muppet.rabbitfriend.core.Message;
import com.muppet.rabbitfriend.core.MessageReply;
import com.muppet.rabbitfriend.core.NeedReplyMessage;
import com.muppet.rabbitfriend.core.RabbitConfiguration;
import com.muppet.rabbitfriend.core.RabbitContext;
import com.muppet.rabbitfriend.core.RabbitFriendUtilExtension;
import com.muppet.rabbitfriend.core.RoutingKey;
import com.muppet.rabbitfriend.core.RpcConsumer;
import com.muppet.rabbitfriend.core.RpcProducer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import static com.muppet.util.GsonUtil.*;

import java.util.Scanner;
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


        context.registerConsumer(new RpcConsumer(context) {

            @Override
            public void handle(Message message) {
                logger.info("成功收到消息" + message.getId());
                if (message instanceof NeedReplyMessage) {
                    NeedReplyMessage m = message.cast();
                    //回复消息

                    m.reply(new AMessageReply());
                }
            }

            public String getQueueName() {
                return "vv";
            }

        }.setExceptionHandler((message, throwable) -> {

        }));
        logger.info("消费者启动");
        new Scanner(System.in).nextLine();

    }

    @Test
    public void test1() {

        CountDownLatch latch = new CountDownLatch(1);
        /***
         * 配置
         */
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

        BaseExchange exchange = context.declareExchangeIfabsent("b", ExchangeType.topic);
        BaseQueue queue = context.declareQueueIfAbsent("vv");
        context.bind(exchange, queue, new RoutingKey("yy"));

        context.registerCallbackErrorHandler(Message.class, (message) -> logger.info(message.getId()));
        context.registerCallbackErrorHandler(CountDownLatch.class, (l) -> {
            latch.countDown();
        });


        /**
         * 创建Producer
         */
        RpcProducer producer = context.createRpcProducer(exchange.getName());
        producer.start();


        /**
         * 注册消费者
         */
        /*context.registerConsumer(new RpcConsumer(context) {

            @Override
            public void handle(Message message) {
                logger.info("成功收到消息" + message.getId());
                message.ack();
                if (message instanceof NeedReplyMessage) {
                    NeedReplyMessage m = message.cast();
                    //回复消息
                    m.reply(new AMessageReply());
                }
            }

            public String getQueueName() {
                return "vv";
            }

        });*/
/**/
        /**
         * 生产者发送消息
         */
        NeedReplyMessage message = new NeedReplyMessage();
        message.setTimeout(50000L);
        message.setRoutingkey("yy");
        logger.info("成功发送消息{}", message.getId());
        producer.send(message, new AsyncMessageReplyCallback(latch, message) {
            @Override
            public void run(MessageReply r) {

                //接受回复
                logger.info("收到返回值");
                logger.info("成功收到返回值, {},{},{}", r.getSuccess(), r.getError().getErrorInfo(), r.getError().getHeaders());
                throw new NullPointerException();
            }
        }, null);

        try {
            latch.await();
        } catch (Exception e) {

        }
    }

    class AMessageReply extends MessageReply {
        {
            success = true;
            error = new CommonErrorCode();
        }
    }


}

class CommonErrorCode extends ErrorCode {
    {
        setErrorInfo("哈哈哈");
    }
}
