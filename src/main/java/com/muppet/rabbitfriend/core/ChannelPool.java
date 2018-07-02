package com.muppet.rabbitfriend.core;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ReturnListener;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by yuhaiqiang on 2018/6/29.
 *
 * @description
 */
public class ChannelPool {
    BlockingQueue<Channel> pool;

    ChannelPool(int size, Connection connection) {
        try {
            pool = new ArrayBlockingQueue<Channel>(size);
            for (int i = 0; i < size; i++) {
                Channel chan = connection.createChannel();
                pool.add(chan);
                chan.addReturnListener(new ReturnListener() {
                    @Override
                    public void handleReturn(int i, String s, String s2, String s3, AMQP.BasicProperties basicProperties, byte[] bytes) throws IOException {
                        //TODO 如果mandatory, immediate 时，消息会返回给生产者，生产需要如何处理
                    }
                });
            }
        } catch (Exception e) {
            throw new RabbitFriendException(e);
        }
    }

    Channel acquire() {
        try {
            Channel chan = pool.poll(1, TimeUnit.MINUTES);
            if (chan == null) {
                String errmsg = "annot get a channel after 1 minutes";
                throw new RabbitFriendException(errmsg);
            }
            return chan;
        } catch (InterruptedException e) {
            throw new RabbitFriendException(e);
        }
    }

    void returnChannel(Channel chan) {
        pool.add(chan);
    }

    void destruct() throws IOException {
        for (Channel chan : pool) {
            try {
                chan.close();
            } catch (IOException e) {
                chan.abort();
            }
        }
    }

}
