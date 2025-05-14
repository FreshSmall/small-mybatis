package com.demo.mybatis;

import cn.hutool.json.JSONUtil;
import com.demo.mybatis.builder.xml.XMLConfigBuilder;
import com.demo.mybatis.io.Resources;
import com.demo.mybatis.session.Configuration;
import com.demo.mybatis.session.SqlSession;
import com.demo.mybatis.session.SqlSessionFactory;
import com.demo.mybatis.session.SqlSessionFactoryBuilder;
import com.demo.mybatis.session.defaults.DefaultSqlSession;
import org.junit.Test;

import java.io.IOException;
import java.io.Reader;

import static cn.hutool.http.ContentType.JSON;

public class ApiTest {

    @Test
    public void test_SqlSessionFactory() throws IOException {
        // 1. 从SqlSessionFactory中获取SqlSession
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(Resources.getResourceAsReader("mybatis-config-datasource.xml"));
        SqlSession sqlSession = sqlSessionFactory.openSession();

        // 2. 获取映射器对象
        IUserDao userDao = sqlSession.getMapper(IUserDao.class);

        // 3. 测试验证
        User user = userDao.queryUserInfoById(1L);
        System.out.println("测试结果："+JSONUtil.toJsonStr(user));
    }

    @Test
    public void test_selectOne() throws IOException {
        // 解析 XML
        Reader reader = Resources.getResourceAsReader("mybatis-config-datasource.xml");
        XMLConfigBuilder xmlConfigBuilder = new XMLConfigBuilder(reader);
        Configuration configuration = xmlConfigBuilder.parse();

        // 获取 DefaultSqlSession
        SqlSession sqlSession = new DefaultSqlSession(configuration);

        // 执行查询：默认是一个集合参数
        Object[] req = {1L};
        Object res = sqlSession.selectOne("cn.bugstack.mybatis.test.dao.IUserDao.queryUserInfoById", req);
        System.out.println("测试结果："+JSONUtil.toJsonStr(res));
    }
}
