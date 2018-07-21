package com.muppet.rabbitfriend.core;

import com.google.common.base.Charsets;
import com.google.gson.Gson;
import com.muppet.util.GsonUtil;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Envelope;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.access.method.P;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by yuhaiqiang on 2018/6/28.
 *
 * @description
 */
public class RabbitContext {


    private RabbitConfiguration configuration;

    private RabbitmqDelegateFactory delegateFactory;

    private Logger logger = LogManager.getLogger(this.getClass());

    private Gson gson = GsonUtil.getGson();

    private RabbitmqDelegate defaultDelegate;


    private Map<Class, Consumer<?>> callbackErrorHandler = new HashMap<>();

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

    public <T> void registerCallbackErrorHandler(Class<T> clazz, Consumer<T> consumer) {
        callbackErrorHandler.put(clazz, consumer);
    }


    public RpcProducer createRpcProducer(String exchangeName) {
        return new RpcProducer(this).setExchange(new BaseExchange(exchangeName));
    }


    public ProducerCompositor createProducer(BaseExchange exchange) {
        return new ProducerCompositor(this).setExchange(exchange);
    }

    private RabbitContext(RabbitConfiguration configuration) {
        this.configuration = configuration;
    }


    public static RabbitContext newRabbitContext(RabbitConfiguration rabbitConfiguration) {
        RabbitContext context = new RabbitContext(rabbitConfiguration);
        return context;
    }

    public void start() {
        delegateFactory = new RabbitmqDelegateFactory(this);
        delegateFactory.start();
        BaseQueue defaultReplyQueue = new BaseQueue(getDefaultReplyQueue());
        defaultDelegate = delegateFactory.acquireDelegate();
        defaultDelegate.declareQueueIfAbsent(defaultReplyQueue);
    }


    public void destroy() {

    }

    public void registerProducer() {

    }

    public void declareConfigurableQueue() {

    }

    public void declareConfigurableExchange() {

    }

    public BaseQueue declareQueueIfAbsent(String name) {
        BaseQueue queue = new BaseQueue();
        queue.setName(name);
        declareQueue(queue);
        return queue;
    }

    public BaseQueue declareQueue(BaseQueue queue) {
        defaultDelegate.declareQueue(queue);
        logger.debug("declare queue[{}]", queue.getName());
        return queue;
    }


    public void declareExchange(BaseExchange exchange) {
        //TODO 初始化交换机的默认参数
        channelExecute((channel) -> {
            try {
                com.rabbitmq.client.AMQP.Exchange.DeclareOk declareOk = channel.exchangeDeclare(exchange.getName(), exchange.getType().toString(), exchange.getDurable(), exchange.getAutoDelete(), exchange.getHeaders());
                return exchange;
            } catch (Exception e) {
                throw new RabbitFriendException(e);
                //return null;
            }
        });
        logger.debug("declare exchange[{}]", GsonUtil.toDefaultJson(exchange));
    }

    public <T> T channelExecute(Function<Channel, T> function) {
        return defaultDelegate.channelExecute(function);
    }


    public BaseExchange declareExchangeIfabsent(String name, ExchangeType type) {
        return channelExecute(channel -> {
            BaseExchange exchange = new BaseExchange(name, type);
            //if (!defaultDelegate.exchangeExist(name)) {
            declareExchange(exchange);
            //}
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


    public String getDefaultReplyQueue() {
        return configuration.getDefaultReplyToQueue();
    }


    public MessageConvert getDefaultMessageConvertor() {
        return defaultMessageConvertor;
    }

    public RabbitContext setDefaultMessageConvertor(MessageConvert defaultMessageConvertor) {
        this.defaultMessageConvertor = defaultMessageConvertor;
        return this;
    }

    public void registerConsumer(BaseConsumer consumer) {
        registerConsumer(consumer, consumer.getDelegate() == null);
    }

    public void registerConsuimerCompositor(ConsumerCompositor consumerCompositor) {
        registerConsumer(consumerCompositor);
    }

    public void registerConsumer(BaseConsumer consume, Boolean isDefaultDelegate) {
        consume.start();
        RabbitmqDelegate delegate = null;
        if (isDefaultDelegate) {
            delegate = defaultDelegate;
        } else {
            delegate = consume.getDelegate();
        }
        delegate.channelExecute(channel -> {
            try {
                //consumerTag 看看能不能用
                consume.setChannel(channel);
                channel.basicConsume(consume.getQueueName(), consume.autoAck(), consume);
            } catch (IOException e) {
                throw new RabbitFriendException(e);
            }
            return null;
        });
        logger.debug("register consumer on queue:{},  with args:{}", consume.getQueueName(), consume.getHeaders());
    }

    public void bind(BaseExchange exchange, BaseQueue queue, RoutingKey routingKey) {
        defaultDelegate.channelExecute(channel -> {
            try {
                channel.queueBind(queue.getName(), exchange.getName(), routingKey.getRoutingKey());
            } catch (IOException e) {
                throw new RabbitFriendException(e);
            }
            return null;
        });
    }

    public RabbitmqDelegateFactory getDelegateFactory() {
        return delegateFactory;
    }

    public <T> Consumer<T> getCallbackErrorHandler(Class<T> clazz) {
        return (Consumer<T>) callbackErrorHandler.get(clazz);
    }
}
