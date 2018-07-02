package com.muppet.rabbitfriend.core;

/**
 * Created by yuhaiqiang on 2018/6/28.
 *
 * @description
 */
public interface MessageReply<T> extends Message {
    public Boolean isSuccess();

    public T getError();
}
