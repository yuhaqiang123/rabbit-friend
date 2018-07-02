package com.muppet.rabbitfriend.core;

/**
 * Created by yuhaiqiang on 2018/6/28.
 *
 * @description
 */
public interface Callback<T> {
    public void run(T r);
}
