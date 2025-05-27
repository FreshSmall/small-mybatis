package com.spring.mybatis;

/*
 * @Author: yinchao
 * @Date: 2025-05-27 10:33:29
 * @LastEditors: yinchao
 * @LastEditTime: 2025-05-27 10:44:59
 * @Description:
 */

import com.demo.mybatis.io.Resources;
import com.demo.mybatis.session.SqlSessionFactoryBuilder;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import com.demo.mybatis.session.SqlSessionFactory;

import java.io.Reader;

/**
 * @author: yinchao
 * @ClassName: SqlSessionFactoryBean
 * @Description:
 * @team wuhan operational dev.
 * @date: 2025/5/27 10:33
 */
public class SqlSessionFactoryBean implements FactoryBean<SqlSessionFactory>, InitializingBean {

    private String resource;
    private SqlSessionFactory sqlSessionFactory;

    @Override
    public SqlSessionFactory getObject() throws Exception {
        return sqlSessionFactory;
    }

    @Override
    public Class<?> getObjectType() {
        return SqlSessionFactoryBean.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        try (Reader resourceAsReader = Resources.getResourceAsReader(resource)) {
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(resourceAsReader);
        }
    }

    public void setResource(String resource) {
        this.resource = resource;
    }
}
