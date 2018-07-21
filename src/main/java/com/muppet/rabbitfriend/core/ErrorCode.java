package com.muppet.rabbitfriend.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by yuhaiqiang on 2018/7/8.
 *
 * @description
 */
public class ErrorCode implements HeadersConfigurable {

    private Map<String, Object> errorHeader;

    private String errorInfo;

    public ErrorCode() {

    }

    public String getErrorInfo() {
        return errorInfo;
    }

    public ErrorCode setErrorInfo(String errorInfo) {
        this.errorInfo = errorInfo;
        return this;
    }

    /**
     * 懒创建，需要时候再创建
     *
     * @return
     */
    private Map<String, Object> getHeader() {
        if (errorHeader == null) {
            synchronized (this) {
                if (errorHeader == null) {
                    errorHeader = new HashMap<>();
                }
            }

        }
        return errorHeader;
    }

    @Override
    public Map setHeaderEntry(String key, Object value) {
        getHeader().put(key, value);
        return getHeader();
    }

    @Override
    public Set<String> getEnabledHeaderKeys() {
        return getHeader().keySet();
    }

    @Override
    public Map getHeaders() {
        return getHeader();
    }
}
