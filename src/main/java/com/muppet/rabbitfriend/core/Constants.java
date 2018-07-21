package com.muppet.rabbitfriend.core;

/**
 * Created by yuhaiqiang on 2018/7/14.
 *
 * @description
 */
public class Constants {

    public static final String PREFIX = "com.muppet.rabbitfriend.";

    public static final String HEADER_EXCHANGE_NAME = PREFIX + "exchange.name";

    public static final String RETRY_EXCHANGE_NAME = PREFIX + "retry.exchange.name";

    public static final String DEAD_LETTER_EXCHANGE_SUFFIX = ".dead.letter.exchange";

    public static final String DEAD_LETTER_QUEUE_SUFFIX = ".dead.letter.queue";

    public static final String HEADER_DEFAULT_PREFIX = PREFIX + "header.";

    public static final String HEADER_TIMEOUT_KEY = HEADER_DEFAULT_PREFIX + "timeout";


    public static final String HEADER_RETRY_TIMES_KEY = HEADER_DEFAULT_PREFIX + "curr.retry.times";

    public static final String HEADER_MAX_RETRY_TIMES = HEADER_DEFAULT_PREFIX + "max.retry.times";

    public static final String HEADER_RETRY_INTERVAL_TIME = HEADER_DEFAULT_PREFIX + "retry.interval.time";

    public static final String HEADER_DEFFERED_EXCHANGE_NAME = HEADER_DEFAULT_PREFIX + "deffered.exchange.name";

    public static final String HEADER_DEFFERED_MESSAGE_TIME = HEADER_DEFAULT_PREFIX + "deffered.message.time";

    public static final String HEADER_ROUTING_KEY = HEADER_DEFAULT_PREFIX + "routing.key";

}
