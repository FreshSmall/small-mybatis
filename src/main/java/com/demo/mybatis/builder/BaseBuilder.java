package com.demo.mybatis.builder;

/*
 * @Author: yinchao
 * @Date: 2025-05-06 22:43:50
 * @LastEditors: yinchao
 * @LastEditTime: 2025-05-19 23:09:40
 * @Description: 
 */
import com.demo.mybatis.session.Configuration;
import com.demo.mybatis.type.TypeAliasRegistry;
import com.demo.mybatis.type.TypeHandlerRegistry;

/**
 * @author: yinchao
 * @ClassName: BaseBuilder
 * @Description:
 * @team wuhan operational dev.
 * @date: 2025/5/6 22:43
 */
public abstract class BaseBuilder {

    protected final Configuration configuration;
    protected final TypeAliasRegistry typeAliasRegistry;
    protected final TypeHandlerRegistry typeHandlerRegistry;

    public BaseBuilder(Configuration configuration) {
        this.configuration = configuration;
        this.typeAliasRegistry = configuration.getTypeAliasRegistry();
        this.typeHandlerRegistry = configuration.getTypeHandlerRegistry();
    }

    public Configuration getConfiguration() {
        return configuration;
    }
    
    protected Class<?> resolveAlias(String alias) {
        return typeAliasRegistry.resolveAlias(alias);
    }
}
