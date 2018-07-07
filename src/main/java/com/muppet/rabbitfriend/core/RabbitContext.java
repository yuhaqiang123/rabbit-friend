package com.muppet.rabbitfriend.core;

import com.google.common.base.Charsets;
import com.google.gson.Gson;
import com.muppet.util.GsonUtil;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Address;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Envelope;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.function.Function;

/**
 * Created by yuhaiqiang on 2018/6/28.
 *
 * @description
 */
public class RabbitContext {


    private RabbitConfiguration configuration;

    private RabbitmqDelegate delegate;

    private Logger logger = LogManager.getLogger(this.getClass());

    private Gson gson = GsonUtil.getGson();


    private MessageConvert defaultMessageConvertor = new MessageConvert() {
        @Override
        public Message loads(String s, Envelope envelope, AMQP.BasicProperties basicProperties, byte[] bytes) {
            try {
                Message msg = gson.fromJson(new String(bytes, "UTF-8"), Message.class);
                msg.setBasicProperties(basicProperties);
                return msg;
            } catch (Exception e) {
                throw new RabbitFriendException(e);
            }
        }

        @Override
        public byte[] dump(Message message) {
            return gson.toJson(message).getBytes(Charsets.UTF_8);
        }
    };


    private RabbitContext(RabbitConfiguration configuration) {
        this.configuration = configuration;
    }


    public static RabbitContext newRabbitContext(RabbitConfiguration rabbitConfiguration) {
        RabbitContext context = new RabbitContext(rabbitConfiguration);
        return context;
    }

    public void start() {
        delegate = new RabbitmqDelegate(this);
        BaseQueue defaultReplyQueue = new BaseQueue(getDefaultReplyQueue());
        delegate.declareQueueIfPresent(defaultReplyQueue);
    }


    public void destroy() {

    }

    public void registerProducer() {

    }

    public void declareConfigurableQueue() {

    }

    public void declareConfigurableExchange() {

    }

    public void declareQueue(String name) {
        BaseQueue queue = new BaseQueue();
        queue.setName(name);
        declareQueue(queue);
    }

    public void declareQueue(BaseQueue queue) {
        channelExecute(channel -> {
            try {
                if (delegate.queueExist(queue.getName())) {
                    return queue;
                }
                channel.queueDeclare(queue.getName(), queue.getDurable(), queue.getExclusize(), queue.getAutoDelete(), queue.getArguments());
                return queue;
            } catch (Exception e) {
                throw new RabbitFriendException(e);
            }
        });
    }


    public void declareExchange(BaseExchange exchange) {
        //TODO 初始化交换机的默认参数
        channelExecute((channel) -> {
            try {
                com.rabbitmq.client.AMQP.Exchange.DeclareOk declareOk = channel.exchangeDeclare(exchange.getName(), exchange.getType().toString(), exchange.getDurable(), exchange.getAutoDelete(), null);
                return exchange;
            } catch (IOException e) {
                throw new RabbitFriendException(e);
            }
        });
    }

    public <T> T channelExecute(Function<Channel, T> function) {
        return delegate.channelExecute(function);
    }


    public Exchange declareExchange(String name, ExchangeType type) {
        return channelExecute(channel -> {
            BaseExchange exchange = new BaseExchange(name, type);
            declareExchange(exchange);
            return exchange;
        });
    }

    public RabbitConfiguration getConfiguration() {
        return configuration;
    }

    public RabbitContext setConfiguration(RabbitConfiguration configuration) {
        this.configuration = configuration;
        return this;
    }

    public RabbitmqDelegate getDelegate() {
        return delegate;
    }

    public RabbitContext setDelegate(RabbitmqDelegate delegate) {
        this.delegate = delegate;
        return this;
    }

    public String getDefaultReplyQueue() {
        configuration.getDefaultReplyToQueue();
    }


    public MessageConvert getDefaultMessageConvertor() {
        return defaultMessageConvertor;
    }

    public RabbitContext setDefaultMessageConvertor(MessageConvert defaultMessageConvertor) {
        this.defaultMessageConvertor = defaultMessageConvertor;
        return this;
    }

    public void registerConsumer(BaseConsumer consume) {
        delegate.channelExecute(channel -> {
            try {
                //consumerTag 看看能不能用
                channel.basicConsume(consume.getQueueName(), false, consume);
            } catch (IOException e) {
                throw new RabbitFriendException(e);
            }
            return null;
        });
    }
}
