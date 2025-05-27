package com.spring.mybatis.mapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import com.demo.mybatis.session.SqlSessionFactory;
import com.spring.mybatis.session.SqlSessionTemplate;

/**
 * @author: yinchao
 * @ClassName: MapperFactoryBean
 * @Description: Mapper工厂Bean，为每个Mapper接口创建代理对象
 * 实现FactoryBean接口，支持Spring IoC容器管理
 * @team wuhan operational dev.
 * @date: 2025/5/6 12:00
 */
public class MapperFactoryBean<T> implements FactoryBean<T>, InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(MapperFactoryBean.class);

    /**
     * Mapper接口类
     */
    private Class<T> mapperInterface;

    /**
     * SqlSessionTemplate实例
     */
    private SqlSessionTemplate sqlSessionTemplate;

    /**
     * SqlSessionFactory实例
     */
    private SqlSessionFactory sqlSessionFactory;

    /**
     * 默认构造函数
     */
    public MapperFactoryBean() {
        // 空构造函数
    }

    /**
     * 构造函数
     *
     * @param mapperInterface Mapper接口类
     */
    public MapperFactoryBean(Class<T> mapperInterface) {
        this.mapperInterface = mapperInterface;
    }

    @Override
    public T getObject() throws Exception {
        SqlSessionTemplate template;
        
        if (this.sqlSessionTemplate != null) {
            template = this.sqlSessionTemplate;
        } else if (this.sqlSessionFactory != null) {
            // 如果没有SqlSessionTemplate，使用SqlSessionFactory创建SqlSessionTemplate
            template = new SqlSessionTemplate(this.sqlSessionFactory);
        } else {
            throw new IllegalStateException("Either sqlSessionTemplate or sqlSessionFactory must be set");
        }
        
        // 确保Mapper接口已注册到Configuration中
        com.demo.mybatis.session.Configuration configuration = template.getConfiguration();
        if (!configuration.hasMapper(this.mapperInterface)) {
            logger.debug("Registering mapper interface: {}", this.mapperInterface.getName());
            configuration.addMapper(this.mapperInterface);
        }
        
        return template.getMapper(this.mapperInterface);
    }

    @Override
    public Class<T> getObjectType() {
        return this.mapperInterface;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (this.mapperInterface == null) {
            throw new IllegalArgumentException("Property 'mapperInterface' is required");
        }

        if (this.sqlSessionTemplate == null && this.sqlSessionFactory == null) {
            throw new IllegalArgumentException("Either sqlSessionTemplate or sqlSessionFactory must be set");
        }

        if (!this.mapperInterface.isInterface()) {
            throw new IllegalArgumentException("mapperInterface must be an interface");
        }

        logger.debug("Initialized MapperFactoryBean for interface: {}", this.mapperInterface.getName());
    }

    /**
     * 设置Mapper接口类
     *
     * @param mapperInterface Mapper接口类
     */
    public void setMapperInterface(Class<T> mapperInterface) {
        this.mapperInterface = mapperInterface;
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
     * 设置SqlSessionFactory
     *
     * @param sqlSessionFactory SqlSessionFactory实例
     */
    public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
    }

    /**
     * 获取Mapper接口类
     *
     * @return Mapper接口类
     */
    public Class<T> getMapperInterface() {
        return mapperInterface;
    }

    /**
     * 获取SqlSessionTemplate
     *
     * @return SqlSessionTemplate实例
     */
    public SqlSessionTemplate getSqlSessionTemplate() {
        return sqlSessionTemplate;
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