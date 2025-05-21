package com.demo.mybatis;

import java.io.IOException;
import java.util.logging.Logger;

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
        logger.info("测试结果：" + JSONUtil.toJsonStr(user));
    }

    @Test
    public void test_queryUserInfo() throws IOException {
        // 2. 获取映射器对象
        IUserDao userDao = sqlSession.getMapper(IUserDao.class);
        // 3. 测试验证
        User userParam = new User();
        userParam.setId(126L);
        userParam.setUser_id(12228);
        User user = userDao.queryUserInfo(userParam);
        logger.info("测试结果：" + JSONUtil.toJsonStr(user));
    }

}
