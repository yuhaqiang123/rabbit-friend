package com.muppet.rabbitfriend.core;

/**
 * Created by yuhaiqiang on 2018/6/29.
 *
 * @description
 */
public class RabbitFriendException extends RuntimeException {

    public RabbitFriendException(Throwable th) {
        super(th);
    }

    public RabbitFriendException(String message) {
        super(message);
    }

    public RabbitFriendException(String message, Throwable th) {
        super(message, th);
    }

    protected RabbitFriendException() {
        super();
    }
}
