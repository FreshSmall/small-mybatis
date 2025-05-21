package com.demo.mybatis.type;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/*
 * @Author: yinchao
 * @Date: 2025-05-20 22:44:07
 * @LastEditors: yinchao
 * @LastEditTime: 2025-05-21 18:15:00
 * @Description: 类型处理器基类
 */
public abstract class BaseTypeHandler<T> implements TypeHandler<T> {

    @Override
    public void setParameter(PreparedStatement ps, int i, T parameter, JdbcType jdbcType) throws SQLException {
        if (parameter == null) {
            if (jdbcType == null) {
                throw new TypeException("JDBC requires that the JdbcType must be specified for all nullable parameters.");
            }
            try {
                ps.setNull(i, jdbcType.TYPE_CODE);
            } catch (SQLException e) {
                throw new TypeException("Error setting null for parameter #" + i + " with JdbcType " + jdbcType + " . " +
                        "Try setting a different JdbcType for this parameter or a different jdbcTypeForNull configuration property. " +
                        "Cause: " + e, e);
            }
        } else {
            setNonNullParameter(ps, i, parameter, jdbcType);
        }
    }

    @Override
    public T getResult(ResultSet rs, String columnName) throws SQLException {
        try {
            return getNullableResult(rs, columnName);
        } catch (Exception e) {
            throw new TypeException("Error getting result for column '" + columnName + "'. Cause: " + e, e);
        }
    }

    @Override
    public T getResult(ResultSet rs, int columnIndex) throws SQLException {
        try {
            return getNullableResult(rs, columnIndex);
        } catch (Exception e) {
            throw new TypeException("Error getting result for column #" + columnIndex + ". Cause: " + e, e);
        }
    }

    /**
     * 设置非空参数
     */
    protected abstract void setNonNullParameter(PreparedStatement ps, int i, T parameter, JdbcType jdbcType) throws SQLException;

    /**
     * 获取可为空的结果，通过列名
     */
    protected abstract T getNullableResult(ResultSet rs, String columnName) throws SQLException;

    /**
     * 获取可为空的结果，通过下标
     */
    protected abstract T getNullableResult(ResultSet rs, int columnIndex) throws SQLException;
}
