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
public class LongTypeHandler extends BaseTypeHandler<Long> {

    @Override
    protected void setNonNullParameter(PreparedStatement ps, int i, Long parameter, JdbcType jdbcType) {
        try {
            ps.setLong(i, parameter);
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
 
    
}
