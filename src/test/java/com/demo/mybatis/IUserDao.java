package com.demo.mybatis;

import com.demo.mybatis.annotations.Select;

import java.util.List;

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

}
