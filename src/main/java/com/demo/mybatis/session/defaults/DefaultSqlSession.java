package com.demo.mybatis.session.defaults;

import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import com.demo.mybatis.binding.MapperRegistry;
import com.demo.mybatis.session.SqlSession;

/**
 * @author: yinchao
 * @ClassName: DefaultSqlSession
 * @Description:
 * @team wuhan operational dev.
 * @date: 2025/5/5 22:29
 */
public class DefaultSqlSession implements SqlSession {
    /**
     * 映射器注册机
     */
    private MapperRegistry mapperRegistry;


    public DefaultSqlSession(MapperRegistry mapperRegistry) {
        this.mapperRegistry = mapperRegistry;
    }

    @Override
    public <T> T selectOne(String statement) {
        return (T) ("你被代理了！" + "方法：" + statement);
    }

    @Override
    public <T> T selectOne(String statement, Object parameters) {
        return (T) ("你被代理了！" + "方法：" + statement + " 入参：" + JSONUtil.toJsonStr(parameters));
    }

    @Override
    public <T> T getMapper(Class<T> type) {
        return mapperRegistry.getMapper(type, this);
    }
}
