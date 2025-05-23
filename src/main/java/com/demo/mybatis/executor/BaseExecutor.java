package com.demo.mybatis.executor;

import java.sql.SQLException;
import java.util.List;

import com.demo.mybatis.cache.Cache;
import com.demo.mybatis.cache.CacheKey;
import com.demo.mybatis.cache.impl.PerpetualCache;
import com.demo.mybatis.mapping.BoundSql;
import com.demo.mybatis.mapping.MappedStatement;
import com.demo.mybatis.session.Configuration;
import com.demo.mybatis.session.ResultHandler;
import com.demo.mybatis.transaction.Transaction;

/**
 * Executor的抽象基类，实现了缓存功能
 * 
 * @author mybatis
 */
public abstract class BaseExecutor implements Executor {

    protected final Configuration configuration;
    protected final Transaction transaction;
    protected Cache localCache;
    
    private boolean closed;

    protected BaseExecutor(Configuration configuration, Transaction transaction) {
        this.configuration = configuration;
        this.transaction = transaction;
        this.localCache = new PerpetualCache("LocalCache");
    }

    @Override
    public <E> List<E> query(MappedStatement ms, Object parameter, ResultHandler resultHandler, BoundSql boundSql) {
        CacheKey key = createCacheKey(ms, parameter, boundSql);
        return query(ms, parameter, resultHandler, boundSql, key);
    }

    @SuppressWarnings("unchecked")
    public <E> List<E> query(MappedStatement ms, Object parameter, ResultHandler resultHandler, BoundSql boundSql, CacheKey key) {
        List<E> list;
        try {
            list = resultHandler == null ? (List<E>) localCache.getObject(key) : null;
            if (list != null) {
                // 缓存命中
                return list;
            } else {
                // 缓存未命中，执行查询
                list = doQuery(ms, parameter, resultHandler, boundSql);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error querying database.  Cause: " + e, e);
        }
        
        if (list != null && resultHandler == null) {
            localCache.putObject(key, list);
        }
        return list;
    }

    @Override
    public int update(MappedStatement ms, Object parameter) throws SQLException {
        clearLocalCache();
        return doUpdate(ms, parameter);
    }

    @Override
    public void commit(boolean required) throws SQLException {
        if (closed) {
            throw new RuntimeException("Cannot commit, transaction is already closed");
        }
        clearLocalCache();
        if (required) {
            transaction.commit();
        }
    }

    @Override
    public void rollback(boolean required) throws SQLException {
        if (!closed) {
            try {
                clearLocalCache();
            } finally {
                if (required) {
                    transaction.rollback();
                }
            }
        }
    }

    @Override
    public void close(boolean forceRollback) throws SQLException {
        try {
            try {
                rollback(forceRollback);
            } finally {
                transaction.close();
            }
        } finally {
            localCache = null;
            closed = true;
        }
    }

    @Override
    public void clearLocalCache() {
        if (!closed) {
            localCache.clear();
        }
    }

    @Override
    public CacheKey createCacheKey(MappedStatement ms, Object parameterObject, BoundSql boundSql) {
        if (closed) {
            throw new RuntimeException("Executor was closed.");
        }
        CacheKey cacheKey = new CacheKey();
        cacheKey.update(ms.getId());
        cacheKey.update(boundSql.getSql());
        if (parameterObject != null) {
            cacheKey.update(parameterObject);
        }
        Configuration configuration = ms.getConfiguration();
        if (configuration != null) {
            cacheKey.update(configuration.getEnvironment().getId());
        }
        return cacheKey;
    }

    @Override
    public Transaction getTransaction() {
        if (closed) {
            throw new RuntimeException("Executor was closed.");
        }
        return transaction;
    }

    /**
     * 子类实现具体的查询逻辑
     */
    protected abstract <E> List<E> doQuery(MappedStatement ms, Object parameter, ResultHandler resultHandler, BoundSql boundSql);

    /**
     * 子类实现具体的更新逻辑
     */
    protected abstract int doUpdate(MappedStatement ms, Object parameter) throws SQLException;
} 