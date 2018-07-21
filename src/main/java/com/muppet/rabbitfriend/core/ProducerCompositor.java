package com.muppet.rabbitfriend.core;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yuhaiqiang on 2018/7/14.
 *
 * @description
 */
public class ProducerCompositor extends BaseProducer implements Producer, MessageProducerExtractor {


    public ProducerCompositor(RabbitContext context) {
        super(context);
    }

    private List<BaseProducer> producers = new ArrayList<>();

    private List<MessageProducerExtractor> extractors = new ArrayList<>();

    private RetryMessageProducer retryMessageProducer;

    private RpcProducer rpcProducer;

    private BaseExchange exchange;

    @Override
    public void start() {
        super.start();

        rpcProducer = new RpcProducer(context);
        rpcProducer.setExchange(getExchange());
        rpcProducer.start();

        retryMessageProducer = new RetryMessageProducer(context);
        retryMessageProducer.setExchange(getExchange());
        retryMessageProducer.start();

        producers.add(rpcProducer);
        producers.add(retryMessageProducer);

        DefferedMessageProducerExtractor defferedMessageProducerExtractor = new DefferedMessageProducerExtractor(context, getExchange());

        extractors.add(rpcProducer);
        extractors.add(retryMessageProducer);
        extractors.add(defferedMessageProducerExtractor);
    }

    @Override
    public void extracte(Message message) {
        extractors.stream().forEach((extractor) -> extractor.extracte(message));
    }


    @Override
    public BaseExchange getSendExchange(Message message) {
        for (MessageProducerExtractor extractor : extractors) {
            BaseExchange exchange = extractor.getSendExchange(message);
            if (exchange != null) {
                return exchange;
            }
        }
        return null;
    }

    public void send(NeedReplyMessage message, AsyncMessageReplyCallback callback) {
        extracte(message);
        rpcProducer.send(message, callback, getSendExchange(message));
    }

    public void send(RetriableMessage message) {
        extracte(message.cast());
        retryMessageProducer.send(message, getSendExchange(message.cast()));
    }

    @Override
    public BaseExchange getExchange() {
        return exchange;
    }

    public ProducerCompositor setExchange(BaseExchange exchange) {
        this.exchange = exchange;
        return this;
    }
}
