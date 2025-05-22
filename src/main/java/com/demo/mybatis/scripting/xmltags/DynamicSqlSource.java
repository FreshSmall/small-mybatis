package com.demo.mybatis.scripting.xmltags;

import java.util.Map;

import com.demo.mybatis.builder.SqlSourceBuilder;
import com.demo.mybatis.mapping.BoundSql;
import com.demo.mybatis.mapping.SqlSource;
import com.demo.mybatis.session.Configuration;

/**
 * 动态SQL源，用于在运行时根据参数对象动态生成SQL语句
 */
public class DynamicSqlSource implements SqlSource {
    private final Configuration configuration;
    private final SqlNode rootSqlNode;

    public DynamicSqlSource(Configuration configuration, SqlNode rootSqlNode) {
        this.configuration = configuration;
        this.rootSqlNode = rootSqlNode;
    }

    @Override
    public BoundSql getBoundSql(Object parameterObject) {
        // 创建动态上下文
        DynamicContext context = new DynamicContext(configuration, parameterObject);
        // 应用SQL节点树
        rootSqlNode.apply(context);
        // 使用SqlSourceBuilder解析生成的SQL
        SqlSourceBuilder sqlSourceParser = new SqlSourceBuilder(configuration);
        Class<?> parameterType = parameterObject == null ? Object.class : parameterObject.getClass();
        // 解析#{}占位符
        SqlSource sqlSource = sqlSourceParser.parse(context.getSql(), parameterType, context.getBindings());
        // 获取BoundSql
        BoundSql boundSql = sqlSource.getBoundSql(parameterObject);
        // 添加额外参数
        for (Map.Entry<String, Object> entry : context.getBindings().entrySet()) {
            boundSql.setAdditionalParameter(entry.getKey(), entry.getValue());
        }
        return boundSql;
    }
}
