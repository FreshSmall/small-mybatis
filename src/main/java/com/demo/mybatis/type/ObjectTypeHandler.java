package com.demo.mybatis.type;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/*
 * @Author: yinchao
 * @Date: 2025-05-21 18:47:00
 * @LastEditors: yinchao
 * @LastEditTime: 2025-05-21 18:47:00
 * @Description: 对象类型处理器，用于处理未知类型
 */
public class ObjectTypeHandler extends BaseTypeHandler<Object> {

    @Override
    protected void setNonNullParameter(PreparedStatement ps, int i, Object parameter, JdbcType jdbcType) throws SQLException {
        ps.setObject(i, parameter);
    }

    @Override
    protected Object getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return rs.getObject(columnName);
    }

    @Override
    protected Object getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return rs.getObject(columnIndex);
    }
}
