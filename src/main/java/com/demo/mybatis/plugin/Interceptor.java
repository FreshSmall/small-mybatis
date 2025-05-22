package com.demo.mybatis.plugin;

import java.util.Properties;

/**
 * MyBatis 拦截器接口
 */
public interface Interceptor {
  
    /**
     * 拦截方法，完成插件的核心逻辑
     * @param invocation 调用信息，包含目标对象、方法和参数
     * @return 方法执行结果
     * @throws Throwable 执行过程中可能抛出的异常
     */
    Object intercept(Invocation invocation) throws Throwable;
  
    /**
     * 为目标对象创建代理对象
     * @param target 目标对象
     * @return 代理对象
     */
    Object plugin(Object target);
  
    /**
     * 设置插件的属性，从配置文件中读取
     * @param properties 属性集合
     */
    void setProperties(Properties properties);
}
