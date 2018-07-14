package com.muppet.rabbitfriend.core;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by yuhaiqiang on 2018/7/13.
 *
 * @description
 */
public abstract class AsyncMessageReplyListCallback extends AsyncMessageReplyCallback {

    public AsyncMessageReplyListCallback(Object backup) {
        super(backup);
    }

    public AsyncMessageReplyListCallback(Object backup, Object... backups) {
        super(backup, backups);
    }

    public abstract void done(List<MessageReply> replies);

    public final Boolean isSuccess() {
        return false;
    }

    public abstract void error(ErrorCode errorCode);

}
