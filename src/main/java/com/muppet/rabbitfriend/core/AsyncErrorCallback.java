package com.muppet.rabbitfriend.core;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by yuhaiqiang on 2018/7/13.
 *
 * @description
 */
public abstract class AsyncErrorCallback extends AsyncSafeCallback<ErrorCode> {

    AsyncErrorCallback(Object object) {
        super(object);
    }

    public AsyncErrorCallback(Object object, Object... backups) {
        super(object, backups);
    }
}
