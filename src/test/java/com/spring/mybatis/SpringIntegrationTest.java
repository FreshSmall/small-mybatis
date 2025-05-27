package com.spring.mybatis;

import javax.sql.DataSource;

import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.alibaba.druid.pool.DruidDataSource;
import com.demo.mybatis.IUserDao;
import com.demo.mybatis.User;
import com.spring.mybatis.config.MyBatisConfiguration;
import com.spring.mybatis.mapper.MapperScan;

/**
 * @author: yinchao
 * @ClassName: SpringIntegrationTest
 * @Description: Spring整合测试类，验证MyBatis与Spring的整合效果
 * @team wuhan operational dev.
 * @date: 2025/5/6 16:00
 */
public class SpringIntegrationTest {

    @Configuration
    @MapperScan("com.demo.mybatis")
    static class TestConfiguration extends MyBatisConfiguration {

        @Bean
        public DataSource dataSource() {
            DruidDataSource dataSource = new DruidDataSource();
            dataSource.setUrl("jdbc:mysql://rm-2zenegn5xmw0id30j.mysql.rds.aliyuncs.com:3306/duke?useUnicode=true");
            dataSource.setUsername("duke_rw");
            dataSource.setPassword("Zu9FX4tCpcDAwynx2NG3bejU");
            dataSource.setDriverClassName("com.mysql.jdbc.Driver");
            return dataSource;
        }
    }

    @Test
    public void testSpringIntegration() {
        // 创建Spring应用上下文
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(TestConfiguration.class);

        try {
            // 从Spring容器获取Mapper
            IUserDao userDao = context.getBean(IUserDao.class);

            // 执行查询
            User user = userDao.queryUserInfoById(126L);
            System.out.println("查询结果：" + user);

            System.out.println("Spring整合测试成功！");
        } finally {
            context.close();
        }
    }
} 
