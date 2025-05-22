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

/**
 * 动态SQL测试类
 */
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
    public void test_queryById() throws IOException {
        // 2. 获取映射器对象
        IUserDao mapper = sqlSession.getMapper(IUserDao.class);
        // 4. 测试验证
        User users = mapper.queryUserInfoById(126L);
        System.out.println("测试结果：" + JSONUtil.toJsonStr(users));
    }
}
