package com.demo.mybatis.transaction;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author: yinchao
 * @ClassName: Transaction
 * @Description:
 * @team wuhan operational dev.
 * @date: 2025/5/14 22:47
 */
public interface Transaction {

    void commit() throws SQLException;

    void rollback();

    void close();

    Connection getConnection();
}
