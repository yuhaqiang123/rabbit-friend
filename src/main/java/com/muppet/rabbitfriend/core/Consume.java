package com.muppet.rabbitfriend.core;

/**
 * Created by yuhaiqiang on 2018/7/5.
 *
 * @description
 */
public interface Consume extends Lifecycle, HeadersConfigurable<String> {

    public void handle(Message message);

    //public String getName();

    public String getQueueName();

    /**
     * 预取消息个数
     * http://www.rabbitmq.com/blog/2012/05/11/some-queuing-theory-throughput-latency-and-bandwidth/
     *
     * @return
     */
    public Integer getPreFetchSize();

    public interface AutoAckEnable {
        default boolean autoAck() {
            return true;
        }
    }
}
