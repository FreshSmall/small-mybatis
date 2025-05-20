package com.demo.mybatis.scripting;

import com.demo.mybatis.executor.parameter.ParameterHandler;
import com.demo.mybatis.mapping.BoundSql;
import com.demo.mybatis.mapping.MappedStatement;
import org.dom4j.Element;

import com.demo.mybatis.mapping.SqlSource;
import com.demo.mybatis.session.Configuration;

/*
 * @Author: yinchao
 * @Date: 2025-05-19 22:47:38
 * @LastEditors: yinchao
 * @LastEditTime: 2025-05-19 23:31:20
 * @Description: 
 */
public interface LanguageDriver {

    SqlSource createSqlSource(Configuration configuration, Element script, Class<?> parameterType);

    ParameterHandler createParameterHandler(MappedStatement mappedStatement, Object parameterObject, BoundSql boundSql);
}
