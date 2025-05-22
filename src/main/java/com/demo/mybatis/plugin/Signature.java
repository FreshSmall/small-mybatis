package com.demo.mybatis.plugin;

import java.lang.annotation.*;

/**
 * 拦截方法签名
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({})
public @interface Signature {
    /**
     * 要拦截的类
     */
    Class<?> type();

    /**
     * 要拦截的方法名
     */
    String method();

    /**
     * 方法参数类型
     */
    Class<?>[] args();
}
