package com.muppet.rabbitfriend.core;

import com.rabbitmq.client.Address;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.apache.commons.lang.StringUtils;
import org.springframework.cglib.core.CollectionUtils;

import java.io.IOException;

/**
 * Created by yuhaiqiang on 2018/6/28.
 *
 * @description
 */
public class RabbitConfiguration {
    private String username;

    private String password;

    private String[] ips;

    private Boolean automaticRecoveryEnabled;

    private Integer requestHeartBeat;

    private Integer networkRecoveryInterval;

    private Integer connectionTimeout;

    private Integer channelPoolSize;

    private Connection connection;

    public RabbitContext getRabbitContext() {
        return RabbitContext.newRabbitContext(this);
    }


    public String getUsername() {
        return username;
    }

    public RabbitConfiguration setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public RabbitConfiguration setPassword(String password) {
        this.password = password;
        return this;
    }

    public String[] getIps() {
        return ips;
    }

    public RabbitConfiguration setIps(String[] ips) {
        this.ips = ips;
        return this;
    }

    public Boolean getAutomaticRecoveryEnabled() {
        return automaticRecoveryEnabled;
    }

    public RabbitConfiguration setAutomaticRecoveryEnabled(Boolean automaticRecoveryEnabled) {
        this.automaticRecoveryEnabled = automaticRecoveryEnabled;
        return this;
    }

    public Integer getRequestHeartBeat() {
        return requestHeartBeat;
    }

    public RabbitConfiguration setRequestHeartBeat(Integer requestHeartBeat) {
        this.requestHeartBeat = requestHeartBeat;
        return this;
    }

    public Integer getNetworkRecoveryInterval() {
        return networkRecoveryInterval;
    }

    public RabbitConfiguration setNetworkRecoveryInterval(Integer networkRecoveryInterval) {
        this.networkRecoveryInterval = networkRecoveryInterval;
        return this;
    }

    public Integer getConnectionTimeout() {
        return connectionTimeout;
    }

    public RabbitConfiguration setConnectionTimeout(Integer connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
        return this;
    }

    public Integer getChannelPoolSize() {
        return channelPoolSize;
    }

    public RabbitConfiguration setChannelPoolSize(Integer channelPoolSize) {
        this.channelPoolSize = channelPoolSize;
        return this;
    }

    public void start() {

    }
}
