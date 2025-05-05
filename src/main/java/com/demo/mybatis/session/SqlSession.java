package com.demo.mybatis.session;

/**
 * @author: yinchao
 * @ClassName: SqlSession
 * @Description:
 * @team wuhan operational dev.
 * @date: 2025/5/5 22:27
 */
public interface SqlSession {

    /**
     * 根据指定的SqlID获取一条记录的封装对象
     *
     * @param statement
     * @param <T>
     * @return
     */
    <T> T selectOne(String statement);

    /**
     * 根据指定的SqlID获取一条记录的封装对象，只不过这个方法容许我们可以给sql传递一些参数
     * 一般在实际使用中，这个参数传递的是pojo，或者Map或者ImmutableMap
     *
     * @param statement
     * @param parameters
     * @param <T>
     * @return
     */
    <T> T selectOne(String statement, Object parameters);

    /**
     * 获取映射器
     *
     * @param <T>
     * @return
     */
    <T> T getMapper(Class<T> type);
}
