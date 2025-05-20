package com.demo.mybatis.type;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/*
 * @Author: yinchao
 * @Date: 2025-05-20 22:44:07
 * @LastEditors: yinchao
 * @LastEditTime: 2025-05-20 22:46:28
 * @Description: 
 */
public abstract class BaseTypeHandler<T> implements TypeHandler<T> {

    @Override
    public void setParameter(PreparedStatement ps, int i, T parameter, JdbcType jdbcType) throws SQLException {
        setNonNullParameter(ps, i, parameter, jdbcType);
    }

    protected abstract void setNonNullParameter(PreparedStatement ps, int i, T parameter, JdbcType jdbcType);

}
