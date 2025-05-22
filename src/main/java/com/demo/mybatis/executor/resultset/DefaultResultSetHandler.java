package com.demo.mybatis.executor.resultset;

/*
 * @Author: yinchao
 * @Date: 2025-05-15 22:54:23
 * @LastEditors: yinchao
 * @LastEditTime: 2025-05-21 18:35:00
 * @Description: 默认结果集处理器
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.demo.mybatis.executor.Executor;
import com.demo.mybatis.mapping.BoundSql;
import com.demo.mybatis.mapping.MappedStatement;
import com.demo.mybatis.mapping.ResultMap;
import com.demo.mybatis.mapping.ResultMapping;
import com.demo.mybatis.reflection.MetaClass;
import com.demo.mybatis.reflection.MetaObject;
import com.demo.mybatis.session.Configuration;
import com.demo.mybatis.type.TypeHandler;

public class DefaultResultSetHandler implements ResultSetHandler {

    private final Configuration configuration;
    private final MappedStatement mappedStatement;
    private final BoundSql boundSql;

    public DefaultResultSetHandler(Executor executor, MappedStatement mappedStatement, BoundSql boundSql) {
        this.configuration = mappedStatement.getConfiguration();
        this.mappedStatement = mappedStatement;
        this.boundSql = boundSql;
    }

    @Override
    public <E> List<E> handleResultSets(Statement stmt) throws SQLException {
        // 获取结果集
        ResultSet rs = stmt.getResultSet();
        if (rs == null) {
            return new ArrayList<>();
        }

        // 包装结果集
        ResultSetWrapper rsw = new ResultSetWrapper(rs, configuration);

        // 获取返回类型
        Class<?> resultType = mappedStatement.getResultType();

        // 检查是否有 ResultMap
        String resultMapId = mappedStatement.getResultMap();
        ResultMap resultMap = null;
        if (resultMapId != null) {
            resultMap = configuration.getResultMap(resultMapId);
        }

        // 检查返回类型是否为集合类型
        if (resultType == List.class || resultType == java.util.Collection.class) {
            // 如果是集合类型，则使用 Object.class 作为元素类型
            resultType = Object.class;
        }

        // 处理结果集
        return handleResultSet(rsw, resultType, resultMap);
    }

    private <E> List<E> handleResultSet(ResultSetWrapper rsw, Class<?> resultType, ResultMap resultMap) throws SQLException {
        List<E> resultList = new ArrayList<>();
        ResultSet rs = rsw.getResultSet();

        // 如果resultType为null但有resultMap，使用resultMap中的类型
        if (resultType == null && resultMap != null) {
            resultType = resultMap.getType();
        }

        // 处理基本类型
        if (isPrimitiveOrWrapper(resultType)) {
            return handlePrimitiveTypeResult(rsw, resultType);
        }

        // 处理对象类型
        while (rs.next()) {
            @SuppressWarnings("unchecked")
            E rowObject = (E) handleRowValues(rsw, resultType, resultMap);
            resultList.add(rowObject);
        }

        return resultList;
    }

    private <E> List<E> handleResultSet(ResultSetWrapper rsw, Class<?> resultType) throws SQLException {
        return handleResultSet(rsw, resultType, null);
    }

    private <T> T handleRowValues(ResultSetWrapper rsw, Class<T> resultType, ResultMap resultMap) throws SQLException {
        try {
            // 如果resultType为null但有resultMap，使用resultMap中的类型
            if (resultType == null && resultMap != null) {
                resultType = (Class<T>) resultMap.getType();
            }

            // 确保resultType不为null
            if (resultType == null) {
                throw new RuntimeException("Result type cannot be null");
            }

            // 创建结果对象实例
            T resultObject = resultType.getDeclaredConstructor().newInstance();

            // 创建元对象，用于设置属性值
            MetaObject metaObject = configuration.newMetaObject(resultObject);

            if (resultMap != null) {
                // 使用 ResultMap 进行映射
                applyResultMap(rsw, resultMap, metaObject);
            } else {
                // 使用列名直接映射
                applyColumnNames(rsw, metaObject);
            }

            return resultObject;
        } catch (Exception e) {
            throw new RuntimeException("Error creating result object: " + e.getMessage(), e);
        }
    }

    private <T> T handleRowValues(ResultSetWrapper rsw, Class<T> resultType) throws SQLException {
        return handleRowValues(rsw, resultType, null);
    }

    private void applyResultMap(ResultSetWrapper rsw, ResultMap resultMap, MetaObject metaObject) throws SQLException {
        for (ResultMapping resultMapping : resultMap.getResultMappings()) {
            String column = resultMapping.getColumn();
            String property = resultMapping.getProperty();

            boolean columnExists = rsw.getColumnNames().contains(column.toUpperCase(Locale.ENGLISH));

            if (columnExists) {
                // 检查属性是否存在
                if (metaObject.hasSetter(property)) {
                    Class<?> propertyType = metaObject.getSetterType(property);

                    // 获取对应类型的TypeHandler
                    TypeHandler<?> typeHandler = rsw.getTypeHandler(propertyType, column);

                    // 使用TypeHandler获取值并设置到对象中
                    Object value = typeHandler.getResult(rsw.getResultSet(), column);

                    if (value != null || !propertyType.isPrimitive()) {
                        metaObject.setValue(property, value);
                    }
                }
            }
        }
    }

    private void applyColumnNames(ResultSetWrapper rsw, MetaObject metaObject) throws SQLException {
        // 获取结果集的列名
        List<String> columnNames = rsw.getColumnNames();

        // 遍历所有列，设置对应的属性值
        for (String columnName : columnNames) {
            // 尝试找到对应的属性名
            String propertyName = columnName;

            // 检查属性是否存在
            if (metaObject.hasSetter(propertyName)) {
                Class<?> propertyType = metaObject.getSetterType(propertyName);

                // 获取对应类型的TypeHandler
                TypeHandler<?> typeHandler = rsw.getTypeHandler(propertyType, columnName);

                // 使用TypeHandler获取值并设置到对象中
                Object value = typeHandler.getResult(rsw.getResultSet(), columnName);
                if (value != null || !propertyType.isPrimitive()) {
                    metaObject.setValue(propertyName, value);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <E> List<E> handlePrimitiveTypeResult(ResultSetWrapper rsw, Class<?> resultType) throws SQLException {
        List<E> result = new ArrayList<>();
        ResultSet rs = rsw.getResultSet();

        // 获取TypeHandler
        TypeHandler<?> typeHandler = rsw.getTypeHandler(resultType, rsw.getColumnNames().get(0));

        // 遍历结果集
        while (rs.next()) {
            // 使用TypeHandler获取值
            Object value = typeHandler.getResult(rs, 1);
            result.add((E) value);
        }

        return result;
    }

    private boolean isPrimitiveOrWrapper(Class<?> clazz) {
        // 防止空指针异常
        if (clazz == null) {
            return false;
        }
        return clazz.isPrimitive() ||
               clazz == Boolean.class ||
               clazz == Byte.class ||
               clazz == Character.class ||
               clazz == Short.class ||
               clazz == Integer.class ||
               clazz == Long.class ||
               clazz == Float.class ||
               clazz == Double.class ||
               clazz == String.class;
    }
}
