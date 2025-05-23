package com.demo.mybatis.executor;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.demo.mybatis.cache.CacheKey;
import com.demo.mybatis.cache.TransactionalCache;
import com.demo.mybatis.mapping.BoundSql;
import com.demo.mybatis.mapping.MappedStatement;
import com.demo.mybatis.session.ResultHandler;
import com.demo.mybatis.transaction.Transaction;

/**
 * 缓存执行器，装饰基本执行器，增加二级缓存功能
 */
public class CachingExecutor implements Executor {
    
    private final Executor delegate;
    private final Map<String, TransactionalCache> transactionalCaches = new HashMap<>();
    
    public CachingExecutor(Executor delegate) {
        this.delegate = delegate;
    }
    
    @Override
    public <E> List<E> query(MappedStatement ms, Object parameter, ResultHandler resultHandler, BoundSql boundSql) {
        // 如果该语句没有启用缓存，直接委托给基础执行器
        if (!ms.isCacheEnabled()) {
            return delegate.query(ms, parameter, resultHandler, boundSql);
        }
        
        // 创建缓存键
        CacheKey key = createCacheKey(ms, parameter, boundSql);
        
        // 获取该Mapper的事务缓存
        TransactionalCache cache = getTransactionalCache(ms);
        
        // 尝试从二级缓存获取结果
        @SuppressWarnings("unchecked")
        List<E> list = (List<E>) cache.getObject(key);
        
        if (list == null) {
            // 二级缓存未命中，从一级缓存或数据库获取
            list = delegate.query(ms, parameter, resultHandler, boundSql);
            
            // 将结果放入二级缓存
            cache.putObject(key, list);
        }
        
        return list;
    }
    
    @Override
    public <E> List<E> query(MappedStatement ms, Object parameter, ResultHandler resultHandler, BoundSql boundSql, CacheKey key) {
        // 如果该语句没有启用缓存，直接委托给基础执行器
        if (!ms.isCacheEnabled()) {
            return delegate.query(ms, parameter, resultHandler, boundSql, key);
        }
        
        // 获取该Mapper的事务缓存
        TransactionalCache cache = getTransactionalCache(ms);
        
        // 尝试从二级缓存获取结果
        @SuppressWarnings("unchecked")
        List<E> list = (List<E>) cache.getObject(key);
        
        if (list == null) {
            // 二级缓存未命中，从一级缓存或数据库获取
            list = delegate.query(ms, parameter, resultHandler, boundSql, key);
            
            // 将结果放入二级缓存
            cache.putObject(key, list);
        }
        
        return list;
    }
    
    @Override
    public int update(MappedStatement ms, Object parameter) throws SQLException {
        // 清空相关缓存
        clearCache(ms);
        
        // 执行更新操作
        return delegate.update(ms, parameter);
    }
    
    @Override
    public Transaction getTransaction() {
        return delegate.getTransaction();
    }
    
    @Override
    public void commit(boolean required) throws SQLException {
        // 先提交基础执行器
        delegate.commit(required);
        
        // 提交所有事务缓存
        if (required) {
            for (TransactionalCache cache : transactionalCaches.values()) {
                cache.commit();
            }
        }
    }
    
    @Override
    public void rollback(boolean required) throws SQLException {
        // 先回滚基础执行器
        delegate.rollback(required);
        
        // 回滚所有事务缓存
        if (required) {
            for (TransactionalCache cache : transactionalCaches.values()) {
                cache.rollback();
            }
        }
    }
    
    @Override
    public void close(boolean forceRollback) throws SQLException {
        try {
            if (forceRollback) {
                for (TransactionalCache cache : transactionalCaches.values()) {
                    cache.rollback();
                }
            } else {
                for (TransactionalCache cache : transactionalCaches.values()) {
                    cache.commit();
                }
            }
        } finally {
            transactionalCaches.clear();
            delegate.close(forceRollback);
        }
    }
    
    @Override
    public CacheKey createCacheKey(MappedStatement ms, Object parameterObject, BoundSql boundSql) {
        return delegate.createCacheKey(ms, parameterObject, boundSql);
    }
    
    @Override
    public void clearLocalCache() {
        delegate.clearLocalCache();
    }
    
    /**
     * 获取指定MappedStatement的事务缓存
     */
    private TransactionalCache getTransactionalCache(MappedStatement ms) {
        String cacheId = ms.getCache().getId();
        TransactionalCache cache = transactionalCaches.get(cacheId);
        if (cache == null) {
            cache = new TransactionalCache(ms.getCache());
            transactionalCaches.put(cacheId, cache);
        }
        return cache;
    }
    
    /**
     * 清空指定MappedStatement的缓存
     */
    private void clearCache(MappedStatement ms) {
        if (ms.isCacheEnabled() && ms.getCache() != null) {
            String cacheId = ms.getCache().getId();
            TransactionalCache cache = transactionalCaches.get(cacheId);
            if (cache != null) {
                cache.clear();
            }
        }
    }
}
