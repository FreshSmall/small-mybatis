package com.demo.mybatis.cache;

import java.util.HashMap;
import java.util.Map;

import com.demo.mybatis.cache.impl.PerpetualCache;

/**
 * 缓存管理器，负责管理所有Mapper的二级缓存实例
 */
public class CacheManager {
    
    // Mapper ID -> Cache 实例的映射
    private final Map<String, Cache> mapperCaches = new HashMap<>();
    
    /**
     * 获取或创建 Mapper 对应的二级缓存
     * 
     * @param mapperId Mapper的唯一标识
     * @return 缓存实例
     */
    public Cache getOrCreateCache(String mapperId) {
        Cache cache = mapperCaches.get(mapperId);
        if (cache == null) {
            // 如果不存在，创建一个新的缓存实例
            cache = new PerpetualCache(mapperId);
            mapperCaches.put(mapperId, cache);
        }
        return cache;
    }
    
    /**
     * 清空指定 Mapper 的二级缓存
     * 
     * @param mapperId Mapper的唯一标识
     */
    public void clearCache(String mapperId) {
        Cache cache = mapperCaches.get(mapperId);
        if (cache != null) {
            cache.clear();
        }
    }
    
    /**
     * 清空所有二级缓存
     */
    public void clearAllCache() {
        for (Cache cache : mapperCaches.values()) {
            cache.clear();
        }
    }
}
