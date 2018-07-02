package com.muppet.rabbitfriend.channel;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Address;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.Method;
import com.rabbitmq.client.ShutdownSignalException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by yuhaiqiang on 2018/6/26.
 *
 * @description
 */
public class TestChannel {

    Logger logger = LogManager.getLogger(this.getClass());

    private Channel channel;

    private Connection conn;
    String EXCHANGE = "M";
    String QUEUE = "D";
    String ROUTINGKEY = "Y";
    String ip = "10.96.83.254";

    @Before
    public void testBefore() {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setAutomaticRecoveryEnabled(true);
        factory.setAutomaticRecoveryEnabled(true);
        factory.setRequestedHeartbeat(10);
        factory.setNetworkRecoveryInterval((int) TimeUnit.SECONDS.toMillis(10));
        factory.setConnectionTimeout((int) TimeUnit.SECONDS.toMillis(10));
        factory.setUsername("zstack");

        factory.setPassword("zstack123");


        Connection conn = null;

        try {
            conn = factory.newConnection(Collections.singletonList(Address.parseAddress(ip)).toArray(new Address[]{}));
            final Channel innerChannel = conn.createChannel();
            channel = innerChannel;

            channel.exchangeDeclare(EXCHANGE, "topic");
            channel.queueDeclare(QUEUE, false, false, false, null);
            channel.queueBind(QUEUE, EXCHANGE, ROUTINGKEY);
        } catch (Throwable t) {
            logger.error(t);
        } finally {

        }
    }

    @After
    public void testAfter() {
        try {
            channel.close();
            conn.close();
        } catch (Throwable t) {
        } finally {

        }

    }

    @Test
    public void testRpc() throws IOException {
        AMQP.Exchange.DeclareOk declareOk = channel.exchangeDeclarePassive(EXCHANGE);
        logger.debug(declareOk.protocolMethodName());
    }


    @Test
    public void test1() throws IOException {


        channel.basicConsume(QUEUE, false, new Consumer() {
            public void handleConsumeOk(String consumerTag) {
                logger.debug("handleConsumeOk");
            }

            public void handleCancelOk(String consumerTag) {
                logger.debug("handleCancelOk");
            }

            public void handleCancel(String consumerTag) throws IOException {
                logger.debug("handleCancel");
            }

            public void handleShutdownSignal(String consumerTag, ShutdownSignalException sig) {
                logger.debug("handleShutdownSignal");
            }

            public void handleRecoverOk(String consumerTag) {
                logger.debug("handleRecoverOk");
            }

            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                logger.info("consumerTag:{}, body:{}", consumerTag, new String(body));
                channel.basicAck(envelope.getDeliveryTag(), true);
            }
        });
        channel.basicPublish(EXCHANGE, ROUTINGKEY, null, "yuhaiqiang".getBytes());
        channel.basicPublish(EXCHANGE, ROUTINGKEY, null, "yuhaiqiang".getBytes());
        logger.info("发送完");
    }

    @Test
    public void test2() {
        logger.debug("yuhaiqiang");
    }

}
