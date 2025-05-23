package com.demo.mybatis.executor;

import java.sql.SQLException;
import java.util.List;

import com.demo.mybatis.cache.CacheKey;
import com.demo.mybatis.mapping.BoundSql;
import com.demo.mybatis.mapping.MappedStatement;
import com.demo.mybatis.session.ResultHandler;
import com.demo.mybatis.transaction.Transaction;

public interface Executor {

    ResultHandler NO_RESULT_HANDLER = null;

    <E> List<E> query(MappedStatement ms, Object parameter, ResultHandler resultHandler, BoundSql boundSql);

    <E> List<E> query(MappedStatement ms, Object parameter, ResultHandler resultHandler, BoundSql boundSql, CacheKey key);

    int update(MappedStatement ms, Object parameter) throws SQLException;

    Transaction getTransaction();

    void commit(boolean required) throws SQLException;

    void rollback(boolean required) throws SQLException;

    void close(boolean forceRollback) throws SQLException;

    /**
     * 创建缓存键
     * @param ms MappedStatement
     * @param parameterObject 参数对象
     * @param boundSql BoundSql
     * @return 缓存键
     */
    CacheKey createCacheKey(MappedStatement ms, Object parameterObject, BoundSql boundSql);

    /**
     * 清空一级缓存
     */
    void clearLocalCache();
}
