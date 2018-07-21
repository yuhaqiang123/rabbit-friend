package com.muppet.rabbitfriend.channel;

import com.google.gson.Gson;
import com.muppet.rabbitfriend.core.BaseExchange;
import com.muppet.rabbitfriend.core.BaseQueue;
import com.muppet.rabbitfriend.core.ExchangeType;
import com.muppet.rabbitfriend.core.RabbitConfiguration;
import com.muppet.rabbitfriend.core.RabbitContext;
import com.muppet.rabbitfriend.core.RabbitFriendUtilExtension;
import com.muppet.rabbitfriend.core.RoutingKey;
import com.muppet.util.GsonUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;

import java.util.concurrent.CountDownLatch;

/**
 * Created by yuhaiqiang on 2018/7/20.
 *
 * @description
 */
public class BaseTest {


    protected RabbitContext context;
    protected RabbitConfiguration configuration;

    protected Logger logger = LogManager.getLogger(this.getClass());

    protected BaseExchange exchange;

    protected BaseQueue queue;

    protected Gson gson = GsonUtil.getGson();

    protected CountDownLatch latch = new CountDownLatch(1);

    protected void stop() {
        latch.countDown();
    }

    protected void waitStop() {
        try {
            latch.await();
        } catch (Exception e) {

        }
    }

    @Before
    public void setup() {
        configuration = new RabbitConfiguration();
        RabbitFriendUtilExtension extension = new RabbitFriendUtilExtension();
        configuration.setUuidGenerator(extension);
        configuration.setUsername("muppet");
        configuration.setPassword("muppet");
        configuration.setIps(new String[]{"127.0.0.1"});
        configuration.setChannelPoolSize(20);
        configuration.setDefaultReplyToQueue("DefaultReplyQueue");

        /***
         * 通过context 注册Producer,Consumer
         */
        context = configuration.getRabbitContext();
        context.start();

        exchange = new BaseExchange("BaseExchange", ExchangeType.topic);
        context.declareExchange(exchange);

        queue = context.declareQueueIfAbsent("TestQueue");
        context.bind(exchange, queue, new RoutingKey("TestQueue"));
    }


}
