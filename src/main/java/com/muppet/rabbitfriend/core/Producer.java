package com.muppet.rabbitfriend.core;

/**
 * Created by yuhaiqiang on 2018/6/27.
 *
 * @description
 */
public interface Producer extends Lifecycle {
    public BaseExchange getExchange();

    public RoutingKey getDefaultRoutingkey();

    public String getName();
}
