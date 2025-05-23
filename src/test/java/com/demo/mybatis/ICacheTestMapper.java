package com.demo.mybatis;

/**
 * 缓存测试Mapper接口
 */
public interface ICacheTestMapper {
    
    /**
     * 根据ID查询用户
     * 
     * @param id 用户ID
     * @return 用户对象
     */
    User queryUserById(Long id);
    
    /**
     * 更新用户信息
     * 
     * @param user 用户对象
     * @return 影响的行数
     */
    int updateUser(User user);
}
