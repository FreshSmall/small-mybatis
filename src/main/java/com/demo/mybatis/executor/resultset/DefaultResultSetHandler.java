package com.demo.mybatis.executor.resultset;

/*
 * @Author: yinchao
 * @Date: 2025-05-15 22:54:23
 * @LastEditors: yinchao
 * @LastEditTime: 2025-05-20 08:48:11
 * @Description:
 */

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.demo.mybatis.executor.Executor;
import com.demo.mybatis.mapping.BoundSql;
import com.demo.mybatis.mapping.MappedStatement;

public class DefaultResultSetHandler implements ResultSetHandler {

    private final MappedStatement mappedStatement;
    private final BoundSql boundSql;

    public DefaultResultSetHandler(Executor executor, MappedStatement mappedStatement, BoundSql boundSql) {
        this.mappedStatement = mappedStatement;
        this.boundSql = boundSql;
    }

    @Override
    public <E> List<E> handleResultSets(Statement stmt) throws SQLException {
        // 获取结果集
        ResultSet rs = stmt.getResultSet();
        return result2Object(rs, mappedStatement.getResultType());
    }

    private <T> List<T> result2Object(ResultSet resultSet, Class<?> clazz) {
        try {
            List<T> resultList = new ArrayList<>();
            while (resultSet.next()) {
                T result = (T) clazz.newInstance();
                Field[] fields = clazz.getDeclaredFields();
                for (Field field : fields) {
                    field.setAccessible(true);
                    field.set(result, resultSet.getObject(field.getName()));
                }
                resultList.add(result);
            }
            return resultList;
        } catch (Exception e) {
            throw new RuntimeException("resultSet2Obj error", e);
        }
    }
}
