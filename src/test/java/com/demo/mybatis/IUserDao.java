package com.demo.mybatis;

import java.util.List;

/**
 * @author: yinchao
 * @ClassName: IUserDao
 * @Description:
 * @team wuhan operational dev.
 * @date: 2025/4/28 22:56
 */
public interface IUserDao {

    User queryUserInfoById(Long uId);

    User queryUserInfo(User userParam);

    List<User> queryUserInfoList();

    int updateUserInfo(User req);

    void insertUserInfo(User req);

    int deleteUserInfoByUserId(String userId);

}
