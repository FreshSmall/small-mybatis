package com.demo.mybatis.scripting.xmltags;

import com.demo.mybatis.executor.parameter.ParameterHandler;
import com.demo.mybatis.mapping.BoundSql;
import com.demo.mybatis.mapping.MappedStatement;
import com.demo.mybatis.scripting.defaults.DefaultParameterHandler;
import org.dom4j.Element;

import com.demo.mybatis.mapping.SqlSource;
import com.demo.mybatis.scripting.LanguageDriver;
import com.demo.mybatis.session.Configuration;

/*
 * @Author: yinchao
 * @Date: 2025-05-19 22:50:26
 * @LastEditors: yinchao
 * @LastEditTime: 2025-05-19 23:36:01
 * @Description: 
 */
public class XMLLanguageDriver implements LanguageDriver{

    @Override
    public SqlSource createSqlSource(Configuration configuration, Element script, Class<?> parameterType) {
        // 用XML脚本构建器解析
        XMLScriptBuilder builder = new XMLScriptBuilder(configuration, script, parameterType);
        return builder.parseScriptNode();
    }

    @Override
    public ParameterHandler createParameterHandler(MappedStatement mappedStatement, Object parameterObject, BoundSql boundSql) {
        return new DefaultParameterHandler(mappedStatement, parameterObject, boundSql);

    }

}
