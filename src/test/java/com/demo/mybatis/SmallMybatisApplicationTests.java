package com.demo.mybatis;


import com.demo.mybatis.binding.MapperProxyFactory;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class SmallMybatisApplicationTests {

    @Test
    public void contextLoads() {
        MapperProxyFactory<IUserDao> mapperProxyFactory = new MapperProxyFactory<>(IUserDao.class);
        Map<String, String> sqlSession = new HashMap<>();
        sqlSession.put("com.demo.mybatis.IUserDao.queryUserName", "模拟执行 Mapper.xml 中 SQL 语句的操作：查询用户姓名");
        sqlSession.put("com.demo.mybatis.IUserDao.queryUserAge", "模拟执行 Mapper.xml 中 SQL 语句的操作：查询用户姓名");
        IUserDao userDao = mapperProxyFactory.newInstance(sqlSession);
        String userName = userDao.queryUserName("1001");
        System.out.println(userName);

    }

}
