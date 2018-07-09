package com.muppet.rabbitfriend.core;

/**
 * Created by yuhaiqiang on 2018/7/5.
 *
 * @description
 */
public interface Consume extends Lifecycle, HeadersConfigurable<String> {

    public void handle(Message message);

    //public String getName();

    public BaseQueue getConsumedQueue();

    public interface AutoAckEnable {
        default boolean autoAck() {
            return true;
        }
    }
}
