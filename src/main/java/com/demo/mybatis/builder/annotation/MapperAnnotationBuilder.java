package com.demo.mybatis.builder.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;

import com.demo.mybatis.annotations.Delete;
import com.demo.mybatis.annotations.Insert;
import com.demo.mybatis.annotations.Param;
import com.demo.mybatis.annotations.Select;
import com.demo.mybatis.annotations.Update;
import com.demo.mybatis.builder.BaseBuilder;
import com.demo.mybatis.mapping.MappedStatement;
import com.demo.mybatis.mapping.SqlCommandType;
import com.demo.mybatis.mapping.SqlSource;
import com.demo.mybatis.scripting.AnnotationLanguageDriver;
import com.demo.mybatis.session.Configuration;

/**
 * @author: yinchao
 * @Description: Builder for parsing mapper annotations
 * @date: 2025/5/22
 */
public class MapperAnnotationBuilder extends BaseBuilder {

    private final Class<?> type;
    private final AnnotationLanguageDriver languageDriver;

    public MapperAnnotationBuilder(Configuration configuration, Class<?> type) {
        super(configuration);
        this.type = type;
        this.languageDriver = new AnnotationLanguageDriver();
    }

    /**
     * Parse all annotations in the mapper interface
     */
    public void parse() {
        String namespace = type.getName();
        
        // Parse methods for SQL annotations
        Method[] methods = type.getMethods();
        for (Method method : methods) {
            parseStatement(method, namespace);
        }
    }

    /**
     * Parse SQL annotations on a method
     */
    private void parseStatement(Method method, String namespace) {
        Class<?> returnType = getReturnType(method);
        String methodName = method.getName();
        String statementId = namespace + "." + methodName;
        
        // Check for SQL annotations
        if (method.isAnnotationPresent(Select.class)) {
            parseSelectAnnotation(method, statementId, returnType);
        } else if (method.isAnnotationPresent(Insert.class)) {
            parseInsertAnnotation(method, statementId, returnType);
        } else if (method.isAnnotationPresent(Update.class)) {
            parseUpdateAnnotation(method, statementId, returnType);
        } else if (method.isAnnotationPresent(Delete.class)) {
            parseDeleteAnnotation(method, statementId, returnType);
        }
    }

    private void parseSelectAnnotation(Method method, String statementId, Class<?> returnType) {
        Select selectAnnotation = method.getAnnotation(Select.class);
        String sql = selectAnnotation.value();
        SqlSource sqlSource = languageDriver.createSqlSource(configuration, sql, getParameterType(method));
        
        // Create a MappedStatement for the SELECT annotation
        MappedStatement.Builder statementBuilder = new MappedStatement.Builder(
                configuration, statementId, SqlCommandType.SELECT, sqlSource, returnType);
        
        configuration.addMappedStatement(statementBuilder.build());
    }

    private void parseInsertAnnotation(Method method, String statementId, Class<?> returnType) {
        Insert insertAnnotation = method.getAnnotation(Insert.class);
        String sql = insertAnnotation.value();
        SqlSource sqlSource = languageDriver.createSqlSource(configuration, sql, getParameterType(method));
        
        // Create a MappedStatement for the INSERT annotation
        MappedStatement.Builder statementBuilder = new MappedStatement.Builder(
                configuration, statementId, SqlCommandType.INSERT, sqlSource, returnType);
        
        configuration.addMappedStatement(statementBuilder.build());
    }

    private void parseUpdateAnnotation(Method method, String statementId, Class<?> returnType) {
        Update updateAnnotation = method.getAnnotation(Update.class);
        String sql = updateAnnotation.value();
        SqlSource sqlSource = languageDriver.createSqlSource(configuration, sql, getParameterType(method));
        
        // Create a MappedStatement for the UPDATE annotation
        MappedStatement.Builder statementBuilder = new MappedStatement.Builder(
                configuration, statementId, SqlCommandType.UPDATE, sqlSource, returnType);
        
        configuration.addMappedStatement(statementBuilder.build());
    }

    private void parseDeleteAnnotation(Method method, String statementId, Class<?> returnType) {
        Delete deleteAnnotation = method.getAnnotation(Delete.class);
        String sql = deleteAnnotation.value();
        SqlSource sqlSource = languageDriver.createSqlSource(configuration, sql, getParameterType(method));
        
        // Create a MappedStatement for the DELETE annotation
        MappedStatement.Builder statementBuilder = new MappedStatement.Builder(
                configuration, statementId, SqlCommandType.DELETE, sqlSource, returnType);
        
        configuration.addMappedStatement(statementBuilder.build());
    }

    /**
     * Get the parameter type for a method
     */
    private Class<?> getParameterType(Method method) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length == 0) {
            return null;
        } else if (parameterTypes.length == 1) {
            return parameterTypes[0];
        } else {
            // For multiple parameters, we'll use a Map
            return Object.class;
        }
    }

    /**
     * Get the return type for a method
     */
    private Class<?> getReturnType(Method method) {
        Class<?> returnType = method.getReturnType();
        // For void methods, use Object.class
        return returnType == void.class ? Object.class : returnType;
    }
}
