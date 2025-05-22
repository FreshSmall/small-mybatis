package com.demo.mybatis.plugin;

import java.io.IOException;
import java.util.List;

import org.junit.Test;

import com.demo.mybatis.User;
import com.demo.mybatis.IUserDao;
import com.demo.mybatis.io.Resources;
import com.demo.mybatis.session.SqlSession;
import com.demo.mybatis.session.SqlSessionFactory;
import com.demo.mybatis.session.SqlSessionFactoryBuilder;

import cn.hutool.json.JSONUtil;

/**
 * 插件测试类
 */
public class PluginTest {

    @Test
    public void test_plugin() throws IOException {
        // 1. 从SqlSessionFactory中获取SqlSession
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(Resources.getResourceAsReader("mybatis-plugin-test.xml"));
        SqlSession sqlSession = sqlSessionFactory.openSession();

        // 2. 获取映射器对象
        IUserDao userDao = sqlSession.getMapper(IUserDao.class);
        
        // 3. 测试验证
        User user = userDao.queryUserInfoById(126L);
        System.out.println("测试结果：" + JSONUtil.toJsonStr(user));
    }
}
