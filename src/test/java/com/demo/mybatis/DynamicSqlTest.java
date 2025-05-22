package com.demo.mybatis;

import java.io.IOException;
import java.util.List;
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
public class DynamicSqlTest {

    private static Logger logger = Logger.getLogger(String.valueOf(DynamicSqlTest.class));

    private SqlSession sqlSession;

    @Before
    public void init() throws IOException {
        // 1. 从SqlSessionFactory中获取SqlSession
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(Resources.getResourceAsReader("mybatis-config-datasource.xml"));
        sqlSession = sqlSessionFactory.openSession();
    }

    @Test
    public void test_queryUserByName() throws IOException {
        // 2. 获取映射器对象
        IDynamicSqlMapper mapper = sqlSession.getMapper(IDynamicSqlMapper.class);

        // 3. 创建查询参数
        User param = new User();
        param.setName("测试");
        param.setUserId(1001);
        // 4. 测试验证
        List<User> users = mapper.queryUserByCondition(param);
        System.out.println("测试结果：" + JSONUtil.toJsonStr(users));
    }
}
