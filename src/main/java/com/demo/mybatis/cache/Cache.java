package com.demo.mybatis.cache;

/**
 * 缓存接口，定义缓存的基本操作
 * 
 * @author mybatis
 */
public interface Cache {

    /**
     * 获取缓存的唯一标识符
     * @return 标识符
     */
    String getId();

    /**
     * 往缓存中添加数据
     * @param key 键
     * @param value 值
     */
    void putObject(Object key, Object value);

    /**
     * 从缓存中获取数据
     * @param key 键
     * @return 值
     */
    Object getObject(Object key);

    /**
     * 从缓存中删除指定的数据
     * @param key 键
     * @return 被删除的值
     */
    Object removeObject(Object key);

    /**
     * 清空缓存
     */
    void clear();

    /**
     * 获取缓存的大小
     * @return 缓存项的数量
     */
    int getSize();
} 