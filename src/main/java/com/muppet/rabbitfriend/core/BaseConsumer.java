package com.muppet.rabbitfriend.core;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.muppet.util.GsonTransient;
import com.muppet.util.GsonTypeCoder;
import com.muppet.util.GsonUtil;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;

/**
 * Created by yuhaiqiang on 2018/7/4.
 *
 * @description
 */
public abstract class BaseConsumer implements Consumer, Lifecycle, Consume {

    private RabbitContext context;


    private Gson gson = GsonUtil.getGson();


    @Override
    public void start() {

    }

    @Override
    public void destroy() {

    }

    @Override
    public void handleConsumeOk(String consumerTag) {

    }

    @Override
    public void handleCancelOk(String consumerTag) {

    }

    @Override
    public void handleCancel(String consumerTag) throws IOException {

    }

    @Override
    public void handleShutdownSignal(String consumerTag, ShutdownSignalException sig) {

    }

    @Override
    public void handleRecoverOk(String consumerTag) {

    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
        Message message = context.getDefaultMessageConvertor().loads(consumerTag, envelope, properties, body);
        //Handle message before Interceptor
        handle(message);
    }

    public BaseQueue getConsumedQueue() {
        return new BaseQueue(getQueueName());
    }

    abstract String getQueueName();


    @Override
    public Map<String, String> setHeaderEntry(String key, String value) {
        return null;
    }

    @Override
    public Set<String> getEnabledHeaderKeys() {
        return null;
    }

    @Override
    public Map<String, String> getHeaders() {
        return null;
    }
}
