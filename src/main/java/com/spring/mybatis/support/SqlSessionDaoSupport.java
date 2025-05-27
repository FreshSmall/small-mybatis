package com.spring.mybatis.support;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.support.DaoSupport;

import com.demo.mybatis.session.SqlSessionFactory;
import com.spring.mybatis.session.SqlSessionTemplate;

/**
 * @author: yinchao
 * @ClassName: SqlSessionDaoSupport
 * @Description: SqlSession DAO基础支持类，为DAO层提供SqlSessionTemplate
 * 继承Spring的DaoSupport，提供统一的DAO基础功能
 * @team wuhan operational dev.
 * @date: 2025/5/6 15:00
 */
public abstract class SqlSessionDaoSupport extends DaoSupport implements InitializingBean {

    private SqlSessionTemplate sqlSessionTemplate;

    /**
     * 设置SqlSessionFactory，会自动创建SqlSessionTemplate
     *
     * @param sqlSessionFactory SqlSessionFactory实例
     */
    public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
        if (this.sqlSessionTemplate == null || sqlSessionFactory != this.sqlSessionTemplate.getSqlSessionFactory()) {
            this.sqlSessionTemplate = createSqlSessionTemplate(sqlSessionFactory);
        }
    }

    /**
     * 设置SqlSessionTemplate
     *
     * @param sqlSessionTemplate SqlSessionTemplate实例
     */
    public void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) {
        this.sqlSessionTemplate = sqlSessionTemplate;
    }

    /**
     * 获取SqlSessionTemplate
     *
     * @return SqlSessionTemplate实例
     */
    public SqlSessionTemplate getSqlSessionTemplate() {
        return this.sqlSessionTemplate;
    }

    /**
     * 创建SqlSessionTemplate
     *
     * @param sqlSessionFactory SqlSessionFactory实例
     * @return SqlSessionTemplate实例
     */
    protected SqlSessionTemplate createSqlSessionTemplate(SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

    @Override
    protected void checkDaoConfig() throws IllegalArgumentException {
        if (this.sqlSessionTemplate == null) {
            throw new IllegalArgumentException("'sqlSessionFactory' or 'sqlSessionTemplate' is required");
        }
    }
} 