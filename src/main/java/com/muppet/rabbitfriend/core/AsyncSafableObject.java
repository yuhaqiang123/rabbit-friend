package com.muppet.rabbitfriend.core;

/**
 * Created by yuhaiqiang on 2018/6/28.
 *
 * @description
 */
public interface AsyncSafableObject {
    public void fail(CommonError error);

    public void after();

    public void before();
}
