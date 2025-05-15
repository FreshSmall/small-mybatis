package com.demo.mybatis.datasource.pooled;

import javax.sql.DataSource;

import com.demo.mybatis.datasource.unpooled.UnpooledDataSourceFactory;

/**
 * @author: yinchao
 * @ClassName: PooledDataSourceFactory
 * @Description:
 * @team wuhan operational dev.
 * @date: 2025/5/15 22:05
 */
public class PooledDataSourceFactory extends UnpooledDataSourceFactory {

    @Override
    public DataSource getDataSource() {
        PooledDataSource pooledDataSource = new PooledDataSource();
        pooledDataSource.setDriver(properties.getProperty("driver"));
        pooledDataSource.setUrl(properties.getProperty("url"));
        pooledDataSource.setUsername(properties.getProperty("username"));
        pooledDataSource.setPassword(properties.getProperty("password"));
        return pooledDataSource;
    }
}
