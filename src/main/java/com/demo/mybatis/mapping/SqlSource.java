package com.demo.mybatis.mapping;

/*
 * @Author: yinchao
 * @Date: 2025-05-19 22:55:24
 * @LastEditors: yinchao
 * @LastEditTime: 2025-05-19 22:55:25
 * @Description: 
 */
public interface SqlSource {

    BoundSql getBoundSql(Object parameterObject);
}
