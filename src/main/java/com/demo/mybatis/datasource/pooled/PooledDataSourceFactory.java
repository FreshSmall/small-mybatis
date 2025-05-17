package com.demo.mybatis.datasource.pooled;

import com.demo.mybatis.datasource.unpooled.UnpooledDataSourceFactory;

/**
 * @author: yinchao
 * @ClassName: PooledDataSourceFactory
 * @Description:
 * @team wuhan operational dev.
 * @date: 2025/5/15 22:05
 */
public class PooledDataSourceFactory extends UnpooledDataSourceFactory {

    public PooledDataSourceFactory() {
        this.dataSource = new PooledDataSource();
    }
}
