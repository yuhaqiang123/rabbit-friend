package com.muppet.rabbitfriend.core;

/**
 * Created by yuhaiqiang on 2018/6/27.
 *
 * @description
 */
public class RoutingKey {
    private String routingKey;

    public static final String ALL_ROUTE_KEY_STR = "*";

    public static final RoutingKey ALL_ROUTE_KEY = new RoutingKey(ALL_ROUTE_KEY_STR);

    public RoutingKey(String routingKey) {
        this.routingKey = routingKey;
    }

    public String getRoutingKey() {
        return routingKey;
    }

    public RoutingKey setRoutingKey(String routingKey) {
        this.routingKey = routingKey;
        return this;
    }


    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof RoutingKey) {
            if (((RoutingKey) obj).getRoutingKey().equals(this.getRoutingKey())) {
                return true;
            }
        }
        return false;
    }
}
