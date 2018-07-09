package com.muppet.rabbitfriend.core;

import com.rabbitmq.client.Address;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.util.function.Function;

/**
 * Created by yuhaiqiang on 2018/7/8.
 *
 * @description
 */
public class RabbitmqDelegateFactory {


    private Connection connection;
    private ChannelPool pool;

    private RabbitConfiguration configuration;
    private Integer channelPoolSize;

    private RabbitContext context;


    public RabbitmqDelegateFactory(RabbitContext context) {
        this.context = context;
        configuration = context.getConfiguration();
        channelPoolSize = configuration.getChannelPoolSize();
    }


    public void start() {
        ConnectionFactory connectionFactory = new ConnectionFactory();

        connectionFactory.setAutomaticRecoveryEnabled(configuration.getAutomaticRecoveryEnabled());
        connectionFactory.setConnectionTimeout(configuration.getConnectionTimeout());
        //TODO
        //connectionFactory.setExceptionHandler();
        connectionFactory.setNetworkRecoveryInterval(configuration.getNetworkRecoveryInterval());
        connectionFactory.setRequestedHeartbeat(configuration.getRequestHeartBeat());
        //TODO
        //connectionFactory.setSharedExecutor();
        //TODO
        //connectionFactory.setThreadFactory();
        connectionFactory.setUsername(configuration.getUsername());
        connectionFactory.setPassword(configuration.getPassword());

        //ExcutorService
        //connectionFactory.newConnection();
        try {
            connection = connectionFactory.newConnection(Address.parseAddresses(StringUtils.join(configuration.getIps(), ",")));
            pool = new ChannelPool(channelPoolSize, connection);
        } catch (IOException e) {
            throw new RabbitFriendException(e);
        }

    }


    public <T> T doChannelExecute(Function<Channel, T> function, Channel channel, boolean returnChannel) {
        try {
            //TODO 使用的默认头配置
            T t = function.apply(channel);
            return t;
        } catch (RabbitFriendException e) {
            throw e;
        } finally {
            if (!returnChannel) {
                pool.returnChannel(channel);
            }
        }
    }

    private Channel acquire() {
        return pool.acquire();
    }

    private void release(Channel channel) {
        pool.returnChannel(channel);
    }

    public RabbitmqDelegate acquireDelegate() {
        return new RabbitmqDelegate(context, connection, acquire());
    }

    public void releaseDelegate(RabbitmqDelegate delegate) {
        release(delegate.getDefaultChannel());
    }
}
