package com.muppet.rabbitfriend.core;

import com.muppet.util.GsonTransient;

/**
 * Created by yuhaiqiang on 2018/6/28.
 *
 * @description
 */
public class MessageReply extends Message {


    @GsonTransient
    private transient NeedReplyMessage requestMessage;

    protected Boolean success;

    protected ErrorCode error;

    public Boolean getSuccess() {
        return success;
    }

    public MessageReply setSuccess(Boolean success) {
        this.success = success;
        return this;
    }

    public MessageReply setError(ErrorCode error) {
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

    public ErrorCode getError() {
        return error;
    }


    public String getCorrelationId() {
        return this.getBasicProperties().getCorrelationId();
    }
}
