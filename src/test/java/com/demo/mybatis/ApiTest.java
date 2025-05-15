package com.demo.mybatis;

import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

import com.demo.mybatis.datasource.pooled.PooledDataSource;
import org.junit.Test;

import com.demo.mybatis.builder.xml.XMLConfigBuilder;
import com.demo.mybatis.io.Resources;
import com.demo.mybatis.session.Configuration;
import com.demo.mybatis.session.SqlSession;
import com.demo.mybatis.session.SqlSessionFactory;
import com.demo.mybatis.session.SqlSessionFactoryBuilder;
import com.demo.mybatis.session.defaults.DefaultSqlSession;

import cn.hutool.json.JSONUtil;

public class ApiTest {

    private static Logger logger = Logger.getLogger(String.valueOf(ApiTest.class));

    @Test
    public void test_SqlSessionFactory() throws IOException {
        // 1. 从SqlSessionFactory中获取SqlSession
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(Resources.getResourceAsReader("mybatis-config-datasource.xml"));
        SqlSession sqlSession = sqlSessionFactory.openSession();

        // 2. 获取映射器对象
        IUserDao userDao = sqlSession.getMapper(IUserDao.class);

        // 3. 测试验证
        for (int i = 0; i < 50; i++) {
            User user = userDao.queryUserInfoById(126L);
            logger.info("测试结果：{}" + JSONUtil.toJsonStr(user));
        }
    }

    @Test
    public void test_pooled() throws SQLException, InterruptedException {
        PooledDataSource pooledDataSource = new PooledDataSource();
        pooledDataSource.setDriver("com.mysql.jdbc.Driver");
        pooledDataSource.setUrl("jdbc:mysql://rm-2zenegn5xmw0id30j.mysql.rds.aliyuncs.com:3306/duke?useUnicode=true");
        pooledDataSource.setUsername("duke_rw");
        pooledDataSource.setPassword("Zu9FX4tCpcDAwynx2NG3bejU");
        // 持续获得链接
        while (true) {
            Connection connection = pooledDataSource.getConnection();
            System.out.println(connection);
            Thread.sleep(1000);
            // 注释掉/不注释掉测试
            connection.close();
        }
    }
}
