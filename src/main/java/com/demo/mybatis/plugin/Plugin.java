package com.demo.mybatis.plugin;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 插件代理类，实现InvocationHandler接口
 */
public class Plugin implements InvocationHandler {
  
    // 目标对象
    private final Object target;
    // 拦截器
    private final Interceptor interceptor;
    // 方法签名映射，用于快速查找需要拦截的方法
    private final Map<Class<?>, Set<Method>> signatureMap;
  
    /**
     * 私有构造方法，通过wrap方法创建代理
     */
    private Plugin(Object target, Interceptor interceptor, Map<Class<?>, Set<Method>> signatureMap) {
        this.target = target;
        this.interceptor = interceptor;
        this.signatureMap = signatureMap;
    }
  
    /**
     * 创建代理对象
     */
    public static Object wrap(Object target, Interceptor interceptor) {
        // 获取签名映射
        Map<Class<?>, Set<Method>> signatureMap = getSignatureMap(interceptor);
        // 获取目标类型
        Class<?> type = target.getClass();
        // 获取需要代理的接口
        Class<?>[] interfaces = getAllInterfaces(type, signatureMap);
      
        if (interfaces.length > 0) {
            // 创建动态代理
            return Proxy.newProxyInstance(
                type.getClassLoader(),
                interfaces,
                new Plugin(target, interceptor, signatureMap)
            );
        }
      
        // 如果没有需要代理的接口，直接返回目标对象
        return target;
    }
  
    /**
     * 代理方法调用
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        try {
            // 获取目标类方法签名集合
            Set<Method> methods = signatureMap.get(method.getDeclaringClass());
            // 检查方法是否需要拦截
            if (methods != null && methods.contains(method)) {
                // 调用拦截器的拦截方法
                return interceptor.intercept(new Invocation(target, method, args));
            }
            // 不需要拦截，直接调用目标方法
            return method.invoke(target, args);
        } catch (Exception e) {
            throw e.getCause() != null ? e.getCause() : e;
        }
    }
  
    /**
     * 获取签名映射
     */
    private static Map<Class<?>, Set<Method>> getSignatureMap(Interceptor interceptor) {
        // 解析拦截器类上的Intercepts注解，获取要拦截的方法签名
        Intercepts interceptsAnnotation = interceptor.getClass().getAnnotation(Intercepts.class);
        if (interceptsAnnotation == null) {
            throw new RuntimeException("No @Intercepts annotation was found in interceptor " + interceptor.getClass().getName());
        }
        
        // 创建签名映射
        Map<Class<?>, Set<Method>> signatureMap = new HashMap<>();
        Signature[] sigs = interceptsAnnotation.value();
        
        for (Signature sig : sigs) {
            // 获取要拦截的类
            Class<?> type = sig.type();
            // 获取要拦截的方法名
            String methodName = sig.method();
            // 获取方法参数类型
            Class<?>[] argTypes = sig.args();
            
            try {
                // 获取方法对象
                Method method = type.getMethod(methodName, argTypes);
                // 将方法添加到签名映射中
                Set<Method> methods = signatureMap.computeIfAbsent(type, k -> new HashSet<>());
                methods.add(method);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException("Could not find method on " + type + " named " + methodName + ". Cause: " + e, e);
            }
        }
        
        return signatureMap;
    }
  
    /**
     * 获取目标类实现的所有接口，但只返回配置了拦截的接口
     */
    private static Class<?>[] getAllInterfaces(Class<?> type, Map<Class<?>, Set<Method>> signatureMap) {
        Set<Class<?>> interfaces = new HashSet<>();
      
        // 递归获取所有接口
        while (type != null) {
            for (Class<?> c : type.getInterfaces()) {
                // 只添加配置了拦截的接口
                if (signatureMap.containsKey(c)) {
                    interfaces.add(c);
                }
            }
            type = type.getSuperclass();
        }
      
        return interfaces.toArray(new Class<?>[0]);
    }
}
