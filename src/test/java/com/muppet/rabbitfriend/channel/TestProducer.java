package com.muppet.rabbitfriend.channel;

import com.muppet.rabbitfriend.core.NeedReplyMessage;
import com.muppet.rabbitfriend.core.Producer;
import com.muppet.rabbitfriend.core.RabbitConfiguration;
import com.muppet.rabbitfriend.core.RabbitFriendUtilExtension;
import com.muppet.rabbitfriend.core.RpcProducer;
import com.rabbitmq.client.AMQP;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * Created by yuhaiqiang on 2018/6/28.
 *
 * @description
 */
public class TestProducer {

    @Test
    public void test1() {

        RabbitConfiguration configuration = new RabbitConfiguration();
        RabbitFriendUtilExtension extension = new RabbitFriendUtilExtension();
        configuration.setUuidGenerator(extension);
        String uuid = ((String) configuration.getUuidGenerator().getUuid());
    }
}
