package com.demo.mybatis.builder;

import com.demo.mybatis.session.Configuration;
import com.demo.mybatis.type.TypeAliasRegistry;

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

    public BaseBuilder(Configuration configuration) {
        this.configuration = configuration;
        this.typeAliasRegistry = configuration.getTypeAliasRegistry();
    }

    public Configuration getConfiguration() {
        return configuration;
    }
}
