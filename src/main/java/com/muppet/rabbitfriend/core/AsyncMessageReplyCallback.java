package com.muppet.rabbitfriend.core;

/**
 * Created by yuhaiqiang on 2018/7/13.
 *
 * @description
 */
public abstract class AsyncMessageReplyCallback extends AsyncSafeCallback<MessageReply> {

    public AsyncMessageReplyCallback(Object backup) {
        super(backup);
    }

    public AsyncMessageReplyCallback(Object backup, Object... backups) {
        super(backup, backups);
    }

}
