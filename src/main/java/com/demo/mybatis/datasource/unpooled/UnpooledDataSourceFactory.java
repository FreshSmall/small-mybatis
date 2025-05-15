package com.demo.mybatis.datasource.unpooled;

import java.util.Properties;

import javax.sql.DataSource;

import com.demo.mybatis.datasource.DataSourceFactory;

/**
 * @author: yinchao
 * @ClassName: UnpooledDataSourceFactory
 * @Description:
 * @team wuhan operational dev.
 * @date: 2025/5/15 22:06
 */
public class UnpooledDataSourceFactory implements DataSourceFactory {

    protected Properties properties;

    @Override
    public void setProperties(Properties props) {
        this.properties = props;
    }

    @Override
    public DataSource getDataSource() {
        UnpooledDataSource unpooledDataSource = new UnpooledDataSource();
        unpooledDataSource.setDriver(properties.getProperty("driver"));
        unpooledDataSource.setUrl(properties.getProperty("url"));
        unpooledDataSource.setUsername(properties.getProperty("username"));
        unpooledDataSource.setPassword(properties.getProperty("password"));
        return unpooledDataSource;
    }
}
