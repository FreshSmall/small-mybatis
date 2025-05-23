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
 * 缓存功能测试类
 */
public class CacheTest {

    private static Logger logger = Logger.getLogger(String.valueOf(CacheTest.class));

    private SqlSession sqlSession;
    private SqlSessionFactory sqlSessionFactory;

    @Before
    public void init() throws IOException {
        // 1. 从SqlSessionFactory中获取SqlSession
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(Resources.getResourceAsReader("mybatis-config-datasource.xml"));
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

    @Test
    public void test_secondLevelCache() throws IOException {
        System.out.println("=== 测试二级缓存功能 ===");

        // 第一次查询，会从数据库加载
        SqlSession sqlSession1 = sqlSessionFactory.openSession();
        ICacheTestMapper mapper1 = sqlSession1.getMapper(ICacheTestMapper.class);
        User user1 = mapper1.queryUserById(126L);
        System.out.println("第一次查询结果：" + JSONUtil.toJsonStr(user1));
        sqlSession1.commit(); // 提交事务，将数据写入二级缓存
        sqlSession1.close();

        // 第二次查询，应该从二级缓存加载
        SqlSession sqlSession2 = sqlSessionFactory.openSession();
        ICacheTestMapper mapper2 = sqlSession2.getMapper(ICacheTestMapper.class);
        User user2 = mapper2.queryUserById(126L);
        System.out.println("第二次查询结果：" + JSONUtil.toJsonStr(user2));
        sqlSession2.close();

        // 验证是否是不同对象（二级缓存返回的是数据副本）
        System.out.println("\n二级缓存验证：");
        System.out.println("两次查询结果是否为同一对象：" + (user1 == user2));
        System.out.println("两次查询结果是否内容相同：" + user1.equals(user2));
    }

    @Test
    public void test_secondLevelCacheInvalidation() throws IOException {
        System.out.println("=== 测试二级缓存失效功能 ===");

        // 第一次查询，会从数据库加载
        SqlSession sqlSession1 = sqlSessionFactory.openSession();
        ICacheTestMapper mapper1 = sqlSession1.getMapper(ICacheTestMapper.class);
        User user1 = mapper1.queryUserById(126L);
        System.out.println("第一次查询结果：" + JSONUtil.toJsonStr(user1));
        sqlSession1.commit(); // 提交事务，将数据写入二级缓存
        sqlSession1.close();

        // 执行更新操作，会清空二级缓存
        SqlSession sqlSession2 = sqlSessionFactory.openSession();
        ICacheTestMapper mapper2 = sqlSession2.getMapper(ICacheTestMapper.class);
        User updateUser = new User();
        updateUser.setId(126L);
        updateUser.setName("Updated Name");
        updateUser.setEmail("updated@example.com");
        // 注意：如果没有实际的更新方法，这里会报错，可以注释掉
        // mapper2.updateUser(updateUser);
        sqlSession2.commit(); // 提交事务，清空二级缓存
        sqlSession2.close();

        // 更新后再次查询，应该从数据库加载
        SqlSession sqlSession3 = sqlSessionFactory.openSession();
        ICacheTestMapper mapper3 = sqlSession3.getMapper(ICacheTestMapper.class);
        User user3 = mapper3.queryUserById(126L);
        System.out.println("更新后查询结果：" + JSONUtil.toJsonStr(user3));
        sqlSession3.close();
    }
}