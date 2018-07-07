package com.muppet.util;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;

/**
 * Created by yuhaiqiang on 2018/7/4.
 *
 * @description
 */
public interface GsonTypeCoder<T> extends JsonDeserializer<T>, JsonSerializer<T> {

}