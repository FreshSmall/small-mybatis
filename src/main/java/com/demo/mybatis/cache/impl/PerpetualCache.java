package com.demo.mybatis.cache.impl;

import java.util.HashMap;
import java.util.Map;

import com.demo.mybatis.cache.Cache;

/**
 * 基于HashMap的永久缓存实现
 * 
 * @author mybatis
 */
public class PerpetualCache implements Cache {

    private final String id;
    private final Map<Object, Object> cache = new HashMap<>();

    public PerpetualCache(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void putObject(Object key, Object value) {
        cache.put(key, value);
    }

    @Override
    public Object getObject(Object key) {
        return cache.get(key);
    }

    @Override
    public Object removeObject(Object key) {
        return cache.remove(key);
    }

    @Override
    public void clear() {
        cache.clear();
    }

    @Override
    public int getSize() {
        return cache.size();
    }

    @Override
    public boolean equals(Object o) {
        if (getId() != null) {
            return getId().equals(o);
        }
        throw new RuntimeException("Cache instances require an ID.");
    }

    @Override
    public int hashCode() {
        if (getId() != null) {
            return getId().hashCode();
        }
        throw new RuntimeException("Cache instances require an ID.");
    }
} 