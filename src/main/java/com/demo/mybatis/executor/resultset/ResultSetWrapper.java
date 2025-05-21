package com.demo.mybatis.executor.resultset;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.demo.mybatis.session.Configuration;
import com.demo.mybatis.type.JdbcType;
import com.demo.mybatis.type.TypeHandler;
import com.demo.mybatis.type.TypeHandlerRegistry;

/*
 * @Author: yinchao
 * @Date: 2025-05-21 18:25:00
 * @LastEditors: yinchao
 * @LastEditTime: 2025-05-21 18:25:00
 * @Description: 结果集包装器
 */
public class ResultSetWrapper {
    private final ResultSet resultSet;
    private final TypeHandlerRegistry typeHandlerRegistry;
    private final List<String> columnNames = new ArrayList<>();
    private final List<String> classNames = new ArrayList<>();
    private final List<JdbcType> jdbcTypes = new ArrayList<>();
    private final Map<String, Map<Class<?>, TypeHandler<?>>> typeHandlerMap = new HashMap<>();
    private final Map<String, List<String>> mappedColumnNamesMap = new HashMap<>();
    private final Map<String, List<String>> unMappedColumnNamesMap = new HashMap<>();

    public ResultSetWrapper(ResultSet rs, Configuration configuration) throws SQLException {
        this.typeHandlerRegistry = configuration.getTypeHandlerRegistry();
        this.resultSet = rs;
        final ResultSetMetaData metaData = rs.getMetaData();
        final int columnCount = metaData.getColumnCount();
        for (int i = 1; i <= columnCount; i++) {
            columnNames.add(metaData.getColumnLabel(i));
            jdbcTypes.add(JdbcType.forCode(metaData.getColumnType(i)));
            classNames.add(metaData.getColumnClassName(i));
        }
    }

    public ResultSet getResultSet() {
        return resultSet;
    }

    public List<String> getColumnNames() {
        return this.columnNames;
    }

    public List<String> getClassNames() {
        return Collections.unmodifiableList(classNames);
    }

    public JdbcType getJdbcType(String columnName) {
        for (int i = 0; i < columnNames.size(); i++) {
            if (columnNames.get(i).equalsIgnoreCase(columnName)) {
                return jdbcTypes.get(i);
            }
        }
        return null;
    }

    /**
     * 获取指定列的TypeHandler
     */
    public <T> TypeHandler<T> getTypeHandler(Class<T> propertyType, String columnName) {
        TypeHandler<T> handler = null;
        Map<Class<?>, TypeHandler<?>> columnHandlers = typeHandlerMap.get(columnName);
        if (columnHandlers == null) {
            columnHandlers = new HashMap<>();
            typeHandlerMap.put(columnName, columnHandlers);
        } else {
            handler = (TypeHandler<T>) columnHandlers.get(propertyType);
        }
        if (handler == null) {
            JdbcType jdbcType = getJdbcType(columnName);
            handler = (TypeHandler<T>) typeHandlerRegistry.getTypeHandler(propertyType, jdbcType);
            if (handler == null) {
                handler = (TypeHandler<T>) typeHandlerRegistry.getTypeHandler(propertyType);
            }
            if (handler == null) {
                // 默认使用Object类型处理器
                handler = (TypeHandler<T>) typeHandlerRegistry.getTypeHandler(Object.class);
            }
            columnHandlers.put(propertyType, handler);
        }
        return handler;
    }

    /**
     * 获取列名（不区分大小写）
     */
    private String getColumnNameByIndex(int i) {
        return columnNames.get(i);
    }

    /**
     * 获取列的索引（不区分大小写）
     */
    public int getColumnIndex(String columnName) {
        for (int i = 0; i < columnNames.size(); i++) {
            if (columnNames.get(i).equalsIgnoreCase(columnName)) {
                return i + 1;
            }
        }
        return -1;
    }

    /**
     * 获取列的类名
     */
    public String getClassNameByIndex(int i) {
        return classNames.get(i);
    }
}
