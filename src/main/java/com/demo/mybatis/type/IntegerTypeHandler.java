package com.demo.mybatis.type;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/*
 * @Author: yinchao
 * @Date: 2025-05-20 22:42:29
 * @LastEditors: yinchao
 * @LastEditTime: 2025-05-20 22:46:48
 * @Description: 
 */
public class IntegerTypeHandler extends BaseTypeHandler<Integer> {

    @Override
    protected void setNonNullParameter(PreparedStatement ps, int i, Integer parameter, JdbcType jdbcType) {
        try {
            ps.setInt(i, parameter);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
 
    
}
