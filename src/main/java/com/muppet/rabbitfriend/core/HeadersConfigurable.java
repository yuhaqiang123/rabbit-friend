package com.muppet.rabbitfriend.core;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by yuhaiqiang on 2018/6/29.
 *
 * @description
 */
public interface HeadersConfigurable {
    public void setHeaders(String key, String value);

    public Set<String> getEnabledHeaderKeys();

    public Map<String, String> getHeaders();
}
