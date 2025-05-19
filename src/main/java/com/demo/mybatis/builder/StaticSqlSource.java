package com.demo.mybatis.builder;

import java.util.List;

import com.demo.mybatis.mapping.BoundSql;
import com.demo.mybatis.mapping.ParameterMapping;
import com.demo.mybatis.mapping.SqlSource;
import com.demo.mybatis.session.Configuration;

/*
 * @Author: yinchao
 * @Date: 2025-05-19 22:45:53
 * @LastEditors: yinchao
 * @LastEditTime: 2025-05-19 23:24:25
 * @Description: 
 */
public class StaticSqlSource implements SqlSource {
    
    private String sql;
    private List<ParameterMapping> parameterMappings;
    private Configuration configuration;

    public StaticSqlSource(Configuration configuration, String sql) {
        this(configuration, sql, null);
    }

    public StaticSqlSource(Configuration configuration, String sql, List<ParameterMapping> parameterMappings) {
        this.sql = sql;
        this.parameterMappings = parameterMappings;
        this.configuration = configuration;
    }

    @Override
    public BoundSql getBoundSql(Object parameterObject) {
        return new BoundSql(configuration, sql, parameterMappings, parameterObject);
    }

}
