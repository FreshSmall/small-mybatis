package com.spring.mybatis.session;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.support.PersistenceExceptionTranslator;

import com.demo.mybatis.session.Configuration;
import com.demo.mybatis.session.SqlSession;
import com.demo.mybatis.session.SqlSessionFactory;
import com.spring.mybatis.support.MyBatisExceptionTranslator;

/**
 * @author: yinchao
 * @ClassName: SqlSessionTemplate
 * @Description: 线程安全的SqlSession实现，集成Spring事务管理
 * 作为SqlSession的代理，自动管理SqlSession生命周期并提供异常转换
 * @team wuhan operational dev.
 * @date: 2025/5/6 10:30
 */
public class SqlSessionTemplate implements SqlSession {

    private static final Logger logger = LoggerFactory.getLogger(SqlSessionTemplate.class);

    private final SqlSessionFactory sqlSessionFactory;
    private final PersistenceExceptionTranslator exceptionTranslator;

    /**
     * 构造函数
     *
     * @param sqlSessionFactory SqlSessionFactory实例
     */
    public SqlSessionTemplate(SqlSessionFactory sqlSessionFactory) {
        this(sqlSessionFactory, new MyBatisExceptionTranslator());
    }

    /**
     * 构造函数
     *
     * @param sqlSessionFactory SqlSessionFactory实例
     * @param exceptionTranslator 异常转换器
     */
    public SqlSessionTemplate(SqlSessionFactory sqlSessionFactory, PersistenceExceptionTranslator exceptionTranslator) {
        if (sqlSessionFactory == null) {
            throw new IllegalArgumentException("Property 'sqlSessionFactory' is required");
        }
        this.sqlSessionFactory = sqlSessionFactory;
        this.exceptionTranslator = exceptionTranslator;
    }

    @Override
    public <T> T selectOne(String statement) {
        return this.execute(session -> session.selectOne(statement));
    }

    @Override
    public <T> T selectOne(String statement, Object parameter) {
        return this.execute(session -> session.selectOne(statement, parameter));
    }

    @Override
    public <E> List<E> selectList(String statement) {
        return this.execute(session -> session.selectList(statement));
    }

    @Override
    public <E> List<E> selectList(String statement, Object parameter) {
        return this.execute(session -> session.selectList(statement, parameter));
    }

    @Override
    public int insert(String statement) {
        return this.execute(session -> session.insert(statement));
    }

    @Override
    public int insert(String statement, Object parameter) {
        return this.execute(session -> session.insert(statement, parameter));
    }

    @Override
    public int update(String statement) {
        return this.execute(session -> session.update(statement));
    }

    @Override
    public int update(String statement, Object parameter) {
        return this.execute(session -> session.update(statement, parameter));
    }

    @Override
    public int delete(String statement) {
        return this.execute(session -> session.delete(statement));
    }

    @Override
    public int delete(String statement, Object parameter) {
        return this.execute(session -> session.delete(statement, parameter));
    }

    @Override
    public <T> T getMapper(Class<T> type) {
        return getConfiguration().getMapper(type, this);
    }

    @Override
    public Configuration getConfiguration() {
        return this.sqlSessionFactory.openSession().getConfiguration();
    }

    @Override
    public void commit() {
        // 在Spring管理的事务中，commit由Spring管理
        logger.debug("Commit operation is managed by Spring transaction");
    }

    @Override
    public void rollback() {
        // 在Spring管理的事务中，rollback由Spring管理
        logger.debug("Rollback operation is managed by Spring transaction");
    }

    @Override
    public void close() {
        // SqlSession的关闭由execute方法管理
        logger.debug("Close operation is managed by SqlSessionTemplate");
    }

    @Override
    public void clearCache() {
        this.execute(session -> {
            session.clearCache();
            return null;
        });
    }

    /**
     * 执行数据库操作的模板方法
     *
     * @param action 数据库操作
     * @param <T> 返回类型
     * @return 操作结果
     */
    private <T> T execute(SqlSessionCallback<T> action) {
        SqlSession session = getSqlSession();
        try {
            return action.doInSqlSession(session);
        } catch (RuntimeException e) {
            // 异常转换
            if (exceptionTranslator != null) {
                RuntimeException translated = exceptionTranslator.translateExceptionIfPossible(e);
                if (translated != null) {
                    throw translated;
                }
            }
            throw e;
        } catch (Exception e) {
            // 将检查异常包装为运行时异常
            RuntimeException runtimeEx = new RuntimeException("Error executing SQL operation", e);
            if (exceptionTranslator != null) {
                RuntimeException translated = exceptionTranslator.translateExceptionIfPossible(runtimeEx);
                if (translated != null) {
                    throw translated;
                }
            }
            throw runtimeEx;
        } finally {
            closeSqlSession(session);
        }
    }

    /**
     * 获取SqlSession
     * 优先从Spring事务同步管理器中获取，如果不存在则创建新的
     *
     * @return SqlSession实例
     */
    private SqlSession getSqlSession() {
        // TODO: 集成Spring事务同步管理器
        // 这里简化实现，实际应该从TransactionSynchronizationManager获取
        return sqlSessionFactory.openSession();
    }

    /**
     * 关闭SqlSession
     *
     * @param session SqlSession实例
     */
    private void closeSqlSession(SqlSession session) {
        if (session != null) {
            // TODO: 根据事务状态决定是否关闭
            // 如果是Spring管理的事务，则不应该立即关闭
            try {
                session.close();
            } catch (Exception e) {
                logger.warn("Error closing SqlSession", e);
            }
        }
    }

    /**
     * SqlSession回调接口
     *
     * @param <T> 返回类型
     */
    @FunctionalInterface
    public interface SqlSessionCallback<T> {
        /**
         * 在SqlSession中执行操作
         *
         * @param session SqlSession实例
         * @return 操作结果
         * @throws Exception 操作异常
         */
        T doInSqlSession(SqlSession session) throws Exception;
    }

    /**
     * 获取SqlSessionFactory
     *
     * @return SqlSessionFactory实例
     */
    public SqlSessionFactory getSqlSessionFactory() {
        return sqlSessionFactory;
    }
} 