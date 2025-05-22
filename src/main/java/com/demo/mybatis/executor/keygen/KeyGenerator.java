package com.demo.mybatis.executor.keygen;

import java.sql.Statement;

import com.demo.mybatis.mapping.MappedStatement;

/**
 * 主键生成器接口
 */
public interface KeyGenerator {
    
    /**
     * 处理自动生成的主键
     * 
     * @param statement SQL语句对象
     * @param parameter 参数对象
     * @param mappedStatement 映射语句对象
     */
    void processGeneratedKeys(Statement statement, Object parameter, MappedStatement mappedStatement);
}
