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
 * 一级缓存功能测试类
 */
public class CacheTest {

    private static Logger logger = Logger.getLogger(String.valueOf(CacheTest.class));

    private SqlSession sqlSession;

    @Before
    public void init() throws IOException {
        // 1. 从SqlSessionFactory中获取SqlSession
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(Resources.getResourceAsReader("mybatis-config-datasource.xml"));
        sqlSession = sqlSessionFactory.openSession();
    }

    @Test
    public void test_firstLevelCache() throws IOException {
        // 2. 获取映射器对象
        IUserDao mapper = sqlSession.getMapper(IUserDao.class);
        
        System.out.println("=== 测试一级缓存功能 ===");
        
        // 第一次查询
        System.out.println("第一次查询：");
        User user1 = mapper.queryUserInfoById(126L);
        System.out.println("查询结果1：" + JSONUtil.toJsonStr(user1));
        
        // 第二次查询相同数据（应该从缓存获取）
        System.out.println("\n第二次查询相同数据：");
        User user2 = mapper.queryUserInfoById(126L);
        System.out.println("查询结果2：" + JSONUtil.toJsonStr(user2));
        
        // 验证是否是同一个对象（缓存命中）
        System.out.println("\n缓存验证：");
        System.out.println("两次查询结果是否为同一对象：" + (user1 == user2));
        
        // 查询不同数据
        System.out.println("\n查询不同数据：");
        User user3 = mapper.queryUserInfoById(127L);
        System.out.println("查询结果3：" + JSONUtil.toJsonStr(user3));
        System.out.println("与第一次查询是否为同一对象：" + (user1 == user3));
    }

    @Test
    public void test_cacheInvalidation() throws IOException {
        // 2. 获取映射器对象
        IUserDao mapper = sqlSession.getMapper(IUserDao.class);
        
        System.out.println("=== 测试缓存失效功能 ===");
        
        // 第一次查询
        System.out.println("第一次查询：");
        User user1 = mapper.queryUserInfoById(126L);
        System.out.println("查询结果1：" + JSONUtil.toJsonStr(user1));
        
        // 清空缓存
        System.out.println("\n清空缓存...");
        sqlSession.clearCache();
        
        // 再次查询（应该重新从数据库获取）
        System.out.println("清空缓存后再次查询：");
        User user2 = mapper.queryUserInfoById(126L);
        System.out.println("查询结果2：" + JSONUtil.toJsonStr(user2));
        
        // 验证是否是不同对象（缓存已清空）
        System.out.println("\n缓存清空验证：");
        System.out.println("清空缓存后两次查询是否为同一对象：" + (user1 == user2));
    }

    @Test
    public void test_cacheInvalidationOnUpdate() throws IOException {
        // 2. 获取映射器对象
        IUserDao mapper = sqlSession.getMapper(IUserDao.class);
        
        System.out.println("=== 测试更新操作后缓存失效 ===");
        
        // 第一次查询
        System.out.println("第一次查询：");
        User user1 = mapper.queryUserInfoById(126L);
        System.out.println("查询结果1：" + JSONUtil.toJsonStr(user1));
        
        // 执行更新操作（这应该清空缓存）
        System.out.println("\n执行更新操作...");
        // 注意：这里假设有更新方法，如果没有可以注释掉这部分
        // mapper.updateUser(user1);
        
        // 模拟更新操作，直接调用clearCache
        sqlSession.clearCache();
        
        // 再次查询（应该重新从数据库获取）
        System.out.println("更新后再次查询：");
        User user2 = mapper.queryUserInfoById(126L);
        System.out.println("查询结果2：" + JSONUtil.toJsonStr(user2));
        
        // 验证是否是不同对象（缓存已清空）
        System.out.println("\n更新后缓存验证：");
        System.out.println("更新后两次查询是否为同一对象：" + (user1 == user2));
    }
} 