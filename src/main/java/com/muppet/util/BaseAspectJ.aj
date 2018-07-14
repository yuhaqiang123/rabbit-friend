package com.muppet.util;

import com.muppet.rabbitfriend.core.AsyncSafeCallback;
import com.muppet.rabbitfriend.core.Callback;
import com.muppet.rabbitfriend.core.Message;
import com.muppet.rabbitfriend.core.RabbitContext;
import com.muppet.rabbitfriend.core.RabbitFriendException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Consumer;

/**
 * Created by yuhaiqiang on 2018/7/10.
 * @description
 */
public aspect BaseAspectJ {
    private Logger logger = LogManager.getLogger(this.getClass());


    after (com.muppet.rabbitfriend.core.RpcConsumer consumer): target(consumer) && execution(com.muppet.rabbitfriend.core.RpcConsumer+.new(..)){
        logger.info("ASPECTJ AOP");
    }


    void around (com.muppet.rabbitfriend.core.RpcConsumer consumer): target(consumer) && execution(void com.muppet.rabbitfriend.core.RpcConsumer+.handle(..)){
        ExceptionDSL.throwable(() -> proceed(consumer), null, () -> {
            Object[] args = thisJoinPoint.getArgs();
            for (Object argu : args) {
                if (argu instanceof Message) {
                    ((Message) argu).nack(false);
                }
            }
        });

    }

    public RabbitContext AsyncSafeCallback.context;

    void around (com.muppet.rabbitfriend.core.RpcProducer producer): target(producer) && execution(void com.muppet.rabbitfriend.core.RpcProducer+.send(..)){
        proceed(producer);
        for (Object argu : thisJoinPoint.getArgs()) {
            if (argu instanceof AsyncSafeCallback) {
                ((AsyncSafeCallback) argu).context = producer.getContext();
            }
        }
    }

    void around(com.muppet.rabbitfriend.core.AsyncSafeCallback callback):target(callback)&& execution(void com.muppet.rabbitfriend.core.AsyncSafeCallback+.run(..)) {
        try {
            proceed(callback);
        } catch (Throwable t) {
            for (Object o : callback.backups) {
                if (o == null) {
                    continue;
                }
                Consumer consumer = getCallbackErrorHandler(callback, o.getClass());
                if (consumer == null) {
                    continue;
                }
                consumer.accept(o);
            }
            throw new RabbitFriendException(t);
        }
    }

    private Consumer getCallbackErrorHandler(AsyncSafeCallback callback, Class clazz) {
        Consumer consumer = callback.context.getCallbackErrorHandler(clazz);
        if (consumer == null && clazz.getSuperclass() != null) {
            return getCallbackErrorHandler(callback, clazz.getSuperclass());
        }
        return consumer;
    }

}
