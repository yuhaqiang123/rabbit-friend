package com.muppet.rabbitfriend.core;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by yuhaiqiang on 2018/6/29.
 *
 * @description
 */
public interface HeadersConfigurable<T> {
    public Map<String, T> setHeaderEntry(String key, T value);

    public Set<String> getEnabledHeaderKeys();

    public Map<String, T> getHeaders();
}
