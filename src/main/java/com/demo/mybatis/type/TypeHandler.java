package com.demo.mybatis.type;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/*
 * @Author: yinchao
 * @Date: 2025-05-19 23:00:02
 * @LastEditors: yinchao
 * @LastEditTime: 2025-05-19 23:01:09
 * @Description: 
 */
public interface TypeHandler<T> {

    void setParameter(PreparedStatement ps, int i, T parameter, JdbcType jdbcType) throws SQLException; 
    
}
