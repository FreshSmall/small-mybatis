package com.demo.mybatis.type;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/*
 * @Author: yinchao
 * @Date: 2025-05-20 22:42:17
 * @LastEditors: yinchao
 * @LastEditTime: 2025-05-20 22:42:18
 * @Description: 
 */
public class StringTypeHandler extends BaseTypeHandler<String> {

    @Override
    protected void setNonNullParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType) {
        try {
            ps.setString(i, parameter);
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
}
