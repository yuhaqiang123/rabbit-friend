package com.muppet.rabbitfriend.core;

/**
 * Created by yuhaiqiang on 2018/6/28.
 *
 * @description
 */

/**
 * 可重试消息除了添加可重试次数，也需要添加超时时间
 */
public interface RetriableMessage extends TimeoutMessage {

//    private Integer maxRetryTimes;
//
//    /**
//     * 当前是第几次梳理该请求
//     */
//    private Integer currentRetryTimes;
//
//    private Integer retryInterval;


    public Integer getMaxRetryTimes();


    public Integer getCurrentRetryTimes();


    public Integer getRetryInterval();

}
