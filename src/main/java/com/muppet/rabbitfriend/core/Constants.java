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
}
