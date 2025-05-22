package com.demo.mybatis.plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 拦截器链，维护所有拦截器
 */
public class InterceptorChain {
  
    // 拦截器列表
    private final List<Interceptor> interceptors = new ArrayList<>();
  
    /**
     * 添加拦截器
     */
    public void addInterceptor(Interceptor interceptor) {
        interceptors.add(interceptor);
    }
  
    /**
     * 获取所有拦截器
     */
    public List<Interceptor> getInterceptors() {
        return Collections.unmodifiableList(interceptors);
    }
  
    /**
     * 对目标对象应用所有拦截器
     */
    public Object pluginAll(Object target) {
        for (Interceptor interceptor : interceptors) {
            target = interceptor.plugin(target);
        }
        return target;
    }
}
