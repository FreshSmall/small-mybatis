package com.demo.mybatis;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import com.demo.mybatis.session.Configuration;
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
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(Resources.getResourceAsReader("mybatis-config-datasource.xml"));
        sqlSession = sqlSessionFactory.openSession();
    }


    @Test
    public void test_queryUserInfoList() throws IOException {
        // 2. 获取映射器对象
        IUserDao userDao = sqlSession.getMapper(IUserDao.class);
        // 3. 测试验证
        List<User> user = userDao.queryUserInfoList();
        System.out.println("测试结果：" + JSONUtil.toJsonStr(user));
    }

    @Test
    public void test_queryUserById() throws IOException {
        // 2. 获取映射器对象
        IUserDao userDao = sqlSession.getMapper(IUserDao.class);
        // 3. 测试验证
        User user = userDao.queryUserInfoById(126L);
        System.out.println("测试结果：" + JSONUtil.toJsonStr(user));
    }

    @Test
    public void test_insert() throws IOException {
        // 2. 获取映射器对象
        IUserDao userDao = sqlSession.getMapper(IUserDao.class);
        // 3. 测试验证
        User user = new User();
        user.setName("测试");
        user.setUserId(1001);
        user.setEmail("123456@qq.com");
        userDao.insertUser(user);
        System.out.println("测试结果：" + JSONUtil.toJsonStr(user));
    }

}
