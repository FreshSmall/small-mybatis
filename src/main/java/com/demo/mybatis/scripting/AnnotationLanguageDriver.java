package com.demo.mybatis.scripting;

import java.util.HashMap;

import com.demo.mybatis.scripting.defaults.DefaultParameterHandler;
import org.dom4j.Element;

import com.demo.mybatis.builder.SqlSourceBuilder;
import com.demo.mybatis.executor.parameter.ParameterHandler;
import com.demo.mybatis.mapping.BoundSql;
import com.demo.mybatis.mapping.MappedStatement;
import com.demo.mybatis.mapping.SqlSource;
import com.demo.mybatis.scripting.defaults.RawSqlSource;
import com.demo.mybatis.session.Configuration;

/**
 * @author: yinchao
 * @Description: Language driver for annotation-based SQL
 * @date: 2025/5/22
 */
public class AnnotationLanguageDriver implements LanguageDriver {

    @Override
    public SqlSource createSqlSource(Configuration configuration, Element script, Class<?> parameterType) {
        // This method is not used for annotations
        throw new UnsupportedOperationException("This method is not supported for annotations");
    }

    /**
     * Create a SqlSource from a SQL string (annotation value)
     * 
     * @param configuration The MyBatis configuration
     * @param script The SQL string from the annotation
     * @param parameterType The parameter type
     * @return A SqlSource instance
     */
    public SqlSource createSqlSource(Configuration configuration, String script, Class<?> parameterType) {
        SqlSourceBuilder sqlSourceParser = new SqlSourceBuilder(configuration);
        Class<?> clazz = parameterType == null ? Object.class : parameterType;
        return sqlSourceParser.parse(script, clazz, new HashMap<>());
    }

    @Override
    public ParameterHandler createParameterHandler(MappedStatement mappedStatement, Object parameterObject, BoundSql boundSql) {
        return new DefaultParameterHandler(mappedStatement, parameterObject, boundSql);
    }
}
