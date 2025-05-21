package com.demo.mybatis;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import com.alibaba.fastjson.JSON;
import org.junit.Before;
import org.junit.Test;

import com.demo.mybatis.io.Resources;
import com.demo.mybatis.session.SqlSession;
import com.demo.mybatis.session.SqlSessionFactory;
import com.demo.mybatis.session.SqlSessionFactoryBuilder;

import cn.hutool.json.JSONUtil;

public class ApiTest {

    private static Logger logger = Logger.getLogger(String.valueOf(ApiTest.class));

    private SqlSession sqlSession;

    @Before
    public void init() throws IOException {
        // 1. 从SqlSessionFactory中获取SqlSession
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder()
                .build(Resources.getResourceAsReader("mybatis-config-datasource.xml"));
        sqlSession = sqlSessionFactory.openSession();
    }

    @Test
    public void test_SqlSessionFactory() throws IOException {
        // 2. 获取映射器对象
        IUserDao userDao = sqlSession.getMapper(IUserDao.class);
        // 3. 测试验证
        User user = userDao.queryUserInfoById(126L);
        System.out.println("测试结果：" + JSONUtil.toJsonStr(user));
    }

    @Test
    public void test_insertUserInfo() {
        // 1. 获取映射器对象
        IUserDao userDao = sqlSession.getMapper(IUserDao.class);

        // 2. 测试验证
        User user = new User();
        user.setUser_id(10002);
        user.setName("小白");
        userDao.insertUserInfo(user);
        System.out.println("测试结果："+"Insert OK");

        // 3. 提交事务
        sqlSession.commit();
    }

    @Test
    public void test_deleteUserInfoByUserId() {
        // 1. 获取映射器对象
        IUserDao userDao = sqlSession.getMapper(IUserDao.class);
        // 2. 测试验证
        int count = userDao.deleteUserInfoByUserId("10002");
        System.out.println("测试结果：" + (count == 1));
        // 3. 提交事务
        sqlSession.commit();
    }


    @Test
    public void test_updateUserInfo() {
        // 1. 获取映射器对象
        IUserDao userDao = sqlSession.getMapper(IUserDao.class);
        User user = new User();
        user.setId(126L);
        user.setName("小白");
        // 2. 测试验证
        int count = userDao.updateUserInfo(user);
        System.out.println("测试结果："+ count);
        // 3. 提交事务
        sqlSession.commit();
    }

    @Test
    public void test_queryUserInfoById() {
        // 1. 获取映射器对象
        IUserDao userDao = sqlSession.getMapper(IUserDao.class);
        // 2. 测试验证：基本参数
        User user = userDao.queryUserInfoById(1L);
        System.out.println("测试结果："+JSON.toJSONString(user));
    }

    @Test
    public void test_queryUserInfo() {
        // 1. 获取映射器对象
        IUserDao userDao = sqlSession.getMapper(IUserDao.class);
        User user = new User();
        user.setUser_id(10002);
        user.setName("小白");
        // 2. 测试验证：对象参数
        User userRes = userDao.queryUserInfo(user);
        System.out.println("测试结果："+ JSON.toJSONString(userRes));
    }

    @Test
    public void test_queryUserInfoList() {
        // 1. 获取映射器对象
        IUserDao userDao = sqlSession.getMapper(IUserDao.class);
        // 2. 测试验证：对象参数
        List<User> users = userDao.queryUserInfoList();
        System.out.println("测试结果："+ JSON.toJSONString(users));
    }

}
