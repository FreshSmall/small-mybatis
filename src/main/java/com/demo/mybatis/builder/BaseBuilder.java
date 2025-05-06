package com.demo.mybatis.builder;

import com.demo.mybatis.session.Configuration;

/**
 * @author: yinchao
 * @ClassName: BaseBuilder
 * @Description:
 * @team wuhan operational dev.
 * @date: 2025/5/6 22:43
 */
public abstract class BaseBuilder {

    protected final Configuration configuration;

    public BaseBuilder(Configuration configuration) {
        this.configuration = configuration;
    }

    public Configuration getConfiguration() {
        return configuration;
    }
}
