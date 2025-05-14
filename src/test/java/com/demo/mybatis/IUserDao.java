package com.demo.mybatis;

/**
 * @author: yinchao
 * @ClassName: IUserDao
 * @Description:
 * @team wuhan operational dev.
 * @date: 2025/4/28 22:56
 */
public interface IUserDao {

    User queryUserInfoById(Long uId);

}
