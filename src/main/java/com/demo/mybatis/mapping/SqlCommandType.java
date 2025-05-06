package com.demo.mybatis.mapping;

/**
 * @author: yinchao
 * @ClassName: SqlCommandType
 * @Description:
 * @team wuhan operational dev.
 * @date: 2025/5/6 22:45
 */
public enum SqlCommandType {

    /**
     * 未知
     */
    UNKNOWN,
    /**
     * 插入
     */
    INSERT,
    /**
     * 更新
     */
    UPDATE,
    /**
     * 删除
     */
    DELETE,
    /**
     * 查找
     */
    SELECT;
}
