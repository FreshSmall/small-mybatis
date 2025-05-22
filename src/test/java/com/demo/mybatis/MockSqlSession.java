package com.demo.mybatis;

import java.util.ArrayList;
import java.util.List;

import com.demo.mybatis.session.Configuration;
import com.demo.mybatis.session.SqlSession;

/**
 * Mock implementation of SqlSession for testing
 */
public class MockSqlSession implements SqlSession {

    private final Configuration configuration;

    public MockSqlSession(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public <T> T selectOne(String statement) {
        return selectOne(statement, null);
    }

    @Override
    public <T> T selectOne(String statement, Object parameter) {
        List<T> list = selectList(statement, parameter);
        if (list.size() == 1) {
            return list.get(0);
        } else if (list.size() > 1) {
            throw new RuntimeException("Expected one result (or null) to be returned by selectOne(), but found: " + list.size());
        } else {
            return null;
        }
    }

    @Override
    public <E> List<E> selectList(String statement) {
        return selectList(statement, null);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E> List<E> selectList(String statement, Object parameter) {
        // Mock implementation that returns test data
        if (statement.contains("queryUserInfoList")) {
            List<User> users = new ArrayList<>();

            User user1 = new User();
            user1.setId(1L);
            user1.setUser_id(1001);
            user1.setName("Mock User 1");

            User user2 = new User();
            user2.setId(2L);
            user2.setUser_id(1002);
            user2.setName("Mock User 2");

            users.add(user1);
            users.add(user2);

            return (List<E>) users;
        }

        return new ArrayList<>();
    }

    @Override
    public int insert(String statement) {
        return 0;
    }

    @Override
    public int insert(String statement, Object parameter) {
        return 0;
    }

    @Override
    public int update(String statement) {
        return 0;
    }

    @Override
    public int update(String statement, Object parameter) {
        return 0;
    }

    @Override
    public int delete(String statement) {
        return 0;
    }

    @Override
    public int delete(String statement, Object parameter) {
        return 0;
    }

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }

    @Override
    public <T> T getMapper(Class<T> type) {
        return configuration.getMapper(type, this);
    }

    @Override
    public void commit() {
        // Mock implementation - do nothing
    }
}
