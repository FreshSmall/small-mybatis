package com.spring.mybatis;

import com.demo.mybatis.session.SqlSessionFactory;
import org.springframework.beans.factory.FactoryBean;

/**
 * @author: yinchao
 * @ClassName: MapperFactoryBean
 * @Description:
 * @team wuhan operational dev.
 * @date: 2025/5/27 10:36
 */
public class MapperFactoryBean<T> implements FactoryBean<T> {

    private Class<T> mapperInterface;
    private SqlSessionFactory sqlSessionFactory;

    public MapperFactoryBean(Class<T> mapperInterface, SqlSessionFactory sqlSessionFactory) {
        this.mapperInterface = mapperInterface;
        this.sqlSessionFactory = sqlSessionFactory;
    }

    @Override
    public T getObject() throws Exception {
        // 实现获取Mapper对象的逻辑
        // 这里可以使用MyBatis的SqlSession来获取Mapper实例
        return sqlSessionFactory.openSession().getMapper(mapperInterface);
    }

    @Override
    public Class<?> getObjectType() {
        return mapperInterface;
    }

    @Override
    public boolean isSingleton() {
        return true; // 默认是单例
    }
}
