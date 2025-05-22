package com.demo.mybatis.plugin;

import java.lang.annotation.*;

/**
 * 标记要拦截的方法
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Intercepts {
    Signature[] value();
}
