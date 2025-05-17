package com.demo.mybatis.reflection.invoker;

import java.lang.reflect.InvocationTargetException;

public interface Invoker {

    public Object invoke(Object target, Object[] args) throws IllegalAccessException, InvocationTargetException;

    public Class<?> getType();
}
