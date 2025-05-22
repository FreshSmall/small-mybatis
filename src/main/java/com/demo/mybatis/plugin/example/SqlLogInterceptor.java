package com.demo.mybatis.plugin.example;

import java.sql.Connection;
import java.util.Properties;

import com.demo.mybatis.executor.statement.StatementHandler;
import com.demo.mybatis.mapping.BoundSql;
import com.demo.mybatis.plugin.Interceptor;
import com.demo.mybatis.plugin.Intercepts;
import com.demo.mybatis.plugin.Invocation;
import com.demo.mybatis.plugin.Plugin;
import com.demo.mybatis.plugin.Signature;
import com.demo.mybatis.reflection.MetaObject;
import com.demo.mybatis.reflection.SystemMetaObject;

/**
 * SQL日志拦截器示例
 * 用于记录SQL执行日志
 */
@Intercepts({
    @Signature(
        type = StatementHandler.class,
        method = "prepare",
        args = {Connection.class}
    )
})
public class SqlLogInterceptor implements Interceptor {

    private Properties properties;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        // 获取StatementHandler
        StatementHandler statementHandler = (StatementHandler) invocation.getTarget();

        // 获取MetaObject，便于操作属性
        MetaObject metaObject = SystemMetaObject.forObject(statementHandler);

        // 获取BoundSql
        BoundSql boundSql = (BoundSql) metaObject.getValue("boundSql");

        // 获取SQL
        String sql = boundSql.getSql();

        // 获取参数
        Object parameterObject = boundSql.getParameterObject();

        // 打印SQL日志
        System.out.println("=============SQL日志=============");
        System.out.println("SQL: " + sql);
        System.out.println("参数: " + parameterObject);
        System.out.println("================================");

        // 继续执行原方法
        return invocation.proceed();
    }

    @Override
    public Object plugin(Object target) {
        // 使用Plugin工具类创建代理
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
        this.properties = properties;
    }
}
