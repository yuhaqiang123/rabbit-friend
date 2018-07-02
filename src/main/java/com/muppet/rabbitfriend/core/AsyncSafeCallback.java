package com.muppet.rabbitfriend.core;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by yuhaiqiang on 2018/6/28.
 *
 * @description
 */
abstract class AsyncSafeCallback implements Callback<MessageReply<CommonError>> {

    protected List<AsyncSafableObject> backups = new LinkedList<>();

    AsyncSafeCallback(AsyncSafableObject object) {
        this.backups.add(object);
    }

    public AsyncSafeCallback(AsyncSafableObject object, AsyncSafableObject... backups) {
        this.backups.add(object);
        for (AsyncSafableObject backup : backups) {
            this.backups.add(backup);
        }
    }
}

