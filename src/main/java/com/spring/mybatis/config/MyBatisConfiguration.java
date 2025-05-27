package com.spring.mybatis.config;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.demo.mybatis.session.SqlSessionFactory;
import com.spring.mybatis.session.SqlSessionFactoryBean;
import com.spring.mybatis.session.SqlSessionTemplate;

/**
 * @author: yinchao
 * @ClassName: MyBatisConfiguration
 * @Description: MyBatis配置类，提供默认的Bean配置
 * 提供SqlSessionFactory和SqlSessionTemplate的默认配置
 * @team wuhan operational dev.
 * @date: 2025/5/6 15:30
 */
@Configuration
public class MyBatisConfiguration {

    /**
     * 配置SqlSessionFactory Bean
     *
     * @param dataSource 数据源
     * @return SqlSessionFactory实例
     * @throws Exception 配置异常
     */
    @Bean
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(dataSource);
        return factoryBean.getObject();
    }

    /**
     * 配置SqlSessionTemplate Bean
     *
     * @param sqlSessionFactory SqlSessionFactory实例
     * @return SqlSessionTemplate实例
     */
    @Bean
    public SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }
} 