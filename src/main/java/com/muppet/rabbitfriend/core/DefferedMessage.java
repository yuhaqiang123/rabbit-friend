package com.muppet.rabbitfriend.core;

/**
 * Created by yuhaiqiang on 2018/6/28.
 *
 * @description
 */

/**
 * 可延迟消息
 * //TODO 延迟消息对rabbitmq的性能影响比较大,所以优化方案为为用户提供
 * 注册延迟队列的接口，按照一定的路由规则路由到该队列
 */
public interface DefferedMessage {

    public Integer getDefferedTime();
}
