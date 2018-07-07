package com.muppet.rabbitfriend.core;

import java.util.UUID;

/**
 * Created by yuhaiqiang on 2018/7/3.
 *
 * @description
 */
public class RabbitFriendUtilExtension implements UuidGenerate {

    @Override
    public String getUuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
