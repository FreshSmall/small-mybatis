package com.demo.mybatis.dao;

import java.util.List;

import com.demo.mybatis.User;
import com.demo.mybatis.annotations.Select;

/**
 * @author: yinchao
 * @ClassName: IUserDao
 * @Description:
 * @team wuhan operational dev.
 * @date: 2025/4/28 22:56
 */
public interface IUserDao {

    @Select("SELECT id, user_id, name FROM a_user")
    List<User> queryUserInfoList();

    /**
     * 根据 id 查询用户信息
     * 使用 resultMap 进行字段映射
     */
    User queryUserInfoById(Long id);

    /**
     * 更新用户信息
     */
    int updateUserInfo(User user);

}
