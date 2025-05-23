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
 * 测试更新操作后缓存自动清空功能
 */
public class CacheUpdateTest {

    private static Logger logger = Logger.getLogger(String.valueOf(CacheUpdateTest.class));

    private SqlSession sqlSession;

    @Before
    public void init() throws IOException {
        // 1. 从SqlSessionFactory中获取SqlSession
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(Resources.getResourceAsReader("mybatis-config-datasource.xml"));
        sqlSession = sqlSessionFactory.openSession();
    }

    @Test
    public void test_cacheInvalidationAfterUpdate() throws IOException {
        // 2. 获取映射器对象
        IUserDao mapper = sqlSession.getMapper(IUserDao.class);
        
        System.out.println("=== 测试更新操作后缓存自动清空 ===");
        
        // 第一次查询
        System.out.println("第一次查询：");
        User user1 = mapper.queryUserInfoById(126L);
        System.out.println("查询结果1：" + JSONUtil.toJsonStr(user1));
        
        // 第二次查询相同数据（应该从缓存获取）
        System.out.println("\n第二次查询相同数据（验证缓存）：");
        User user2 = mapper.queryUserInfoById(126L);
        System.out.println("查询结果2：" + JSONUtil.toJsonStr(user2));
        System.out.println("两次查询是否为同一对象（缓存命中）：" + (user1 == user2));
        
        // 执行更新操作（这应该自动清空缓存）
        System.out.println("\n执行更新操作...");
        if (user1 != null) {
            user1.setName("更新后的名称");
            user1.setEmail("updated@example.com");
            int updateCount = mapper.updateUserInfo(user1);
            System.out.println("更新影响行数：" + updateCount);
        }
        
        // 再次查询（应该重新从数据库获取，因为缓存已被清空）
        System.out.println("\n更新后再次查询：");
        User user3 = mapper.queryUserInfoById(126L);
        System.out.println("查询结果3：" + JSONUtil.toJsonStr(user3));
        
        // 验证是否是不同对象（缓存已清空）
        System.out.println("\n更新后缓存验证：");
        System.out.println("更新前后查询是否为同一对象：" + (user1 == user3));
        System.out.println("说明：false表示缓存已被清空，重新从数据库查询");
        
        // 回滚事务，避免影响其他测试
        System.out.println("\n回滚事务...");
        sqlSession.rollback();
    }

    @Test
    public void test_commitClearCache() throws IOException {
        // 2. 获取映射器对象
        IUserDao mapper = sqlSession.getMapper(IUserDao.class);
        
        System.out.println("=== 测试commit操作清空缓存 ===");
        
        // 第一次查询
        System.out.println("第一次查询：");
        User user1 = mapper.queryUserInfoById(126L);
        System.out.println("查询结果1：" + JSONUtil.toJsonStr(user1));
        
        // 第二次查询相同数据（应该从缓存获取）
        System.out.println("\n第二次查询相同数据（验证缓存）：");
        User user2 = mapper.queryUserInfoById(126L);
        System.out.println("查询结果2：" + JSONUtil.toJsonStr(user2));
        System.out.println("两次查询是否为同一对象（缓存命中）：" + (user1 == user2));
        
        // 提交事务（这应该清空缓存）
        System.out.println("\n提交事务...");
        sqlSession.commit();
        
        // 再次查询（应该重新从数据库获取）
        System.out.println("提交后再次查询：");
        User user3 = mapper.queryUserInfoById(126L);
        System.out.println("查询结果3：" + JSONUtil.toJsonStr(user3));
        
        // 验证是否是不同对象（缓存已清空）
        System.out.println("\n提交后缓存验证：");
        System.out.println("提交前后查询是否为同一对象：" + (user1 == user3));
        System.out.println("说明：false表示缓存已被清空，重新从数据库查询");
    }

    @Test
    public void test_rollbackClearCache() throws IOException {
        // 2. 获取映射器对象
        IUserDao mapper = sqlSession.getMapper(IUserDao.class);
        
        System.out.println("=== 测试rollback操作清空缓存 ===");
        
        // 第一次查询
        System.out.println("第一次查询：");
        User user1 = mapper.queryUserInfoById(126L);
        System.out.println("查询结果1：" + JSONUtil.toJsonStr(user1));
        
        // 第二次查询相同数据（应该从缓存获取）
        System.out.println("\n第二次查询相同数据（验证缓存）：");
        User user2 = mapper.queryUserInfoById(126L);
        System.out.println("查询结果2：" + JSONUtil.toJsonStr(user2));
        System.out.println("两次查询是否为同一对象（缓存命中）：" + (user1 == user2));
        
        // 回滚事务（这应该清空缓存）
        System.out.println("\n回滚事务...");
        sqlSession.rollback();
        
        // 再次查询（应该重新从数据库获取）
        System.out.println("回滚后再次查询：");
        User user3 = mapper.queryUserInfoById(126L);
        System.out.println("查询结果3：" + JSONUtil.toJsonStr(user3));
        
        // 验证是否是不同对象（缓存已清空）
        System.out.println("\n回滚后缓存验证：");
        System.out.println("回滚前后查询是否为同一对象：" + (user1 == user3));
        System.out.println("说明：false表示缓存已被清空，重新从数据库查询");
    }
} 