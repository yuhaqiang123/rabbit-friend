package com.muppet.rabbitfriend.core;

/**
 * Created by yuhaiqiang on 2018/6/28.
 *
 * @description
 */
public interface DefferedMessage {


    //private Integer defferedTime;

    public DefferedMessage setDefferedTime(Integer defferedTime);

    public Integer getDefferedTime();
}
