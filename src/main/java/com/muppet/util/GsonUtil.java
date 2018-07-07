package com.muppet.util;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.muppet.rabbitfriend.core.Message;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * Created by yuhaiqiang on 2018/7/4.
 *
 * @description
 */
public class GsonUtil implements GsonTypeCoder<Message> {
    GsonBuilder _gsonBuilder;


    public static final Gson gson = new GsonUtil().setCoder(Message.class).setExclusionStrategies(new ExclusionStrategy[]{
            new ExclusionStrategy() {
                @Override
                public boolean shouldSkipField(FieldAttributes fieldAttributes) {
                    return fieldAttributes.getAnnotation(GsonTransient.class) != null;
                }

                @Override
                public boolean shouldSkipClass(Class<?> aClass) {
                    return false;
                }
            }
    }).create();

    @Override
    public Message deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject jObj = jsonElement.getAsJsonObject();
        Map.Entry<String, JsonElement> entry = jObj.entrySet().iterator().next();
        String className = entry.getKey();
        Class<?> clazz;
        try {
            clazz = Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new JsonParseException(String.format("Unable to deserialize class[%s]", className), e);
        }
        return (Message) gson.fromJson(entry.getValue(), clazz);
    }

    @Override
    public JsonElement serialize(Message message, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jObj = new JsonObject();
        jObj.add(message.getClass().getName(), gson.toJsonTree(message));
        return jObj;
    }

    public static Gson getGson() {
        return gson;
    }

    public GsonUtil() {
        _gsonBuilder = new GsonBuilder();
    }

    public GsonUtil setCoder(Class<?> clazz, GsonTypeCoder<?> coder) {
        _gsonBuilder.registerTypeAdapter(clazz, coder);
        return this;
    }

    public GsonUtil setCoder(Class<?> clazz) {
        _gsonBuilder.registerTypeAdapter(clazz, this);
        return this;
    }

    public GsonUtil setExclusionStrategies(ExclusionStrategy[] excludeStrateges) {
        _gsonBuilder.setExclusionStrategies(excludeStrateges);
        return this;
    }

    public GsonUtil setInstanceCreator(Class<?> clazz, InstanceCreator<?> creator) {
        _gsonBuilder.registerTypeAdapter(clazz, creator);
        return this;
    }

    public GsonUtil enableNullDecoder() {
        _gsonBuilder.serializeNulls();
        return this;
    }

    public Gson create() {
        //TODO: configuration database
        _gsonBuilder.setVersion(1.7);
        return _gsonBuilder.create();
    }
}
