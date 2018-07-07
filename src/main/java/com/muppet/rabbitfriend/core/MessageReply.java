package com.muppet.rabbitfriend.core;

/**
 * Created by yuhaiqiang on 2018/6/28.
 *
 * @description
 */
public class MessageReply<T> extends Message {

    private NeedReplyMessage requestMessage;

    private Boolean success;

    private T error;

    public Boolean getSuccess() {
        return success;
    }

    public MessageReply setSuccess(Boolean success) {
        this.success = success;
        return this;
    }

    public MessageReply setError(T error) {
        this.success = false;
        this.error = error;
        return this;
    }

    public NeedReplyMessage getRequestMessage() {
        return requestMessage;
    }

    public MessageReply setRequestMessage(NeedReplyMessage requestMessage) {
        this.requestMessage = requestMessage;
        return this;
    }

    public T getError() {
        return error;
    }


    public String getCorrelationId() {
        return this.getBasicProperties().getCorrelationId();
    }
}
