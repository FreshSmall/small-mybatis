package com.demo.mybatis.scripting.defaults;

import java.util.HashMap;

import com.demo.mybatis.builder.SqlSourceBuilder;
import com.demo.mybatis.mapping.BoundSql;
import com.demo.mybatis.mapping.SqlSource;
import com.demo.mybatis.scripting.xmltags.DynamicContext;
import com.demo.mybatis.scripting.xmltags.SqlNode;
import com.demo.mybatis.session.Configuration;

/*
 * @Author: yinchao
 * @Date: 2025-05-19 22:49:14
 * @LastEditors: yinchao
 * @LastEditTime: 2025-05-19 23:32:19
 * @Description: 
 */
public class RawSqlSource implements SqlSource {
    
    private final SqlSource sqlSource;

    public RawSqlSource(Configuration configuration, SqlNode rootSqlNode, Class<?> parameterType) {
        this(configuration, getSql(configuration, rootSqlNode), parameterType);
    }

    public RawSqlSource(Configuration configuration, String sql, Class<?> parameterType) {
        SqlSourceBuilder sqlSourceParser = new SqlSourceBuilder(configuration);
        Class<?> clazz = parameterType == null ? Object.class : parameterType;
        sqlSource = sqlSourceParser.parse(sql, clazz, new HashMap<>());
    }

    @Override
    public BoundSql getBoundSql(Object parameterObject) {
        return sqlSource.getBoundSql(parameterObject);
    }

    private static String getSql(Configuration configuration, SqlNode rootSqlNode) {
        DynamicContext context = new DynamicContext(configuration, null);
        rootSqlNode.apply(context);
        return context.getSql();
    }
    
}
