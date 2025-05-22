package com.demo.mybatis.plugin;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 调用信息封装类
 */
public class Invocation {
  
    // 目标对象
    private final Object target;
    // 目标方法
    private final Method method;
    // 方法参数
    private final Object[] args;
  
    public Invocation(Object target, Method method, Object[] args) {
        this.target = target;
        this.method = method;
        this.args = args;
    }
  
    // 获取目标对象
    public Object getTarget() {
        return target;
    }
  
    // 获取方法
    public Method getMethod() {
        return method;
    }
  
    // 获取参数
    public Object[] getArgs() {
        return args;
    }
  
    // 执行目标方法
    public Object proceed() throws InvocationTargetException, IllegalAccessException {
        return method.invoke(target, args);
    }
}
