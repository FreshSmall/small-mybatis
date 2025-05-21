package com.demo.mybatis.type;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/*
 * @Author: yinchao
 * @Date: 2025-05-19 23:00:02
 * @LastEditors: yinchao
 * @LastEditTime: 2025-05-21 18:10:00
 * @Description: 类型处理器接口
 */
public interface TypeHandler<T> {

    /**
     * 设置参数
     */
    void setParameter(PreparedStatement ps, int i, T parameter, JdbcType jdbcType) throws SQLException;

    /**
     * 从结果集中获取值，通过列名
     */
    T getResult(ResultSet rs, String columnName) throws SQLException;

    /**
     * 从结果集中获取值，通过下标
     */
    T getResult(ResultSet rs, int columnIndex) throws SQLException;
}
