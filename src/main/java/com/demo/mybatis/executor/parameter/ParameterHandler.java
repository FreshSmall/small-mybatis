package com.demo.mybatis.executor.parameter;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/*
 * @Author: yinchao
 * @Date: 2025-05-20 22:53:55
 * @LastEditors: yinchao
 * @LastEditTime: 2025-05-20 22:53:56
 * @Description: 
 */
public interface ParameterHandler {
   
    /**
     * 获取参数对象
     * @return
     */
    Object getParameterObject();

    /**
     * 设置参数
     * @param ps
     * @throws SQLException
     */
    void setParameters(PreparedStatement ps) throws SQLException;
}
