package com.demo.mybatis.session;

/**
 * @author: yinchao
 * @ClassName: SqlSessionFactory
 * @Description:
 * @team wuhan operational dev.
 * @date: 2025/5/5 22:27
 */
public interface SqlSessionFactory {

    /**
     * 打开一个 session
     *
     * @return
     */
    SqlSession openSession();
}
