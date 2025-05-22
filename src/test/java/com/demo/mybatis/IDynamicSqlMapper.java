package com.demo.mybatis;

import java.util.List;

/**
 * 动态SQL测试接口
 */
public interface IDynamicSqlMapper {

    /**
     * 根据条件查询用户
     * 
     * @param user 查询条件
     * @return 用户列表
     */
    List<User> queryUserByCondition(User user);
}
