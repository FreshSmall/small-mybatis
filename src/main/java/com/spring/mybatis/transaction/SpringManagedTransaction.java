package com.spring.mybatis.transaction;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DataSourceUtils;

import com.demo.mybatis.transaction.Transaction;

/**
 * @author: yinchao
 * @ClassName: SpringManagedTransaction
 * @Description: Spring管理的事务实现，集成Spring事务管理机制
 * 实现small-mybatis的Transaction接口，委托Spring管理事务生命周期
 * @team wuhan operational dev.
 * @date: 2025/5/6 13:00
 */
public class SpringManagedTransaction implements Transaction {

    private static final Logger logger = LoggerFactory.getLogger(SpringManagedTransaction.class);

    private final DataSource dataSource;
    private Connection connection;
    private boolean isConnectionTransactional;
    private boolean autoCommit;

    /**
     * 构造函数
     *
     * @param dataSource 数据源
     */
    public SpringManagedTransaction(DataSource dataSource) {
        if (dataSource == null) {
            throw new IllegalArgumentException("DataSource cannot be null");
        }
        this.dataSource = dataSource;
    }

    @Override
    public Connection getConnection() {
        if (this.connection == null) {
            openConnection();
        }
        return this.connection;
    }

    /**
     * 打开数据库连接
     * 优先从Spring事务同步管理器中获取连接
     */
    private void openConnection() {
        try {
            this.connection = DataSourceUtils.getConnection(this.dataSource);
            this.autoCommit = this.connection.getAutoCommit();
            this.isConnectionTransactional = DataSourceUtils.isConnectionTransactional(this.connection, this.dataSource);

            logger.debug("JDBC Connection [{}] will{} be managed by Spring", 
                this.connection, this.isConnectionTransactional ? "" : " not");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get connection from DataSource", e);
        }
    }

    @Override
    public void commit() throws SQLException {
        if (this.connection != null && !this.isConnectionTransactional && !this.autoCommit) {
            logger.debug("Committing JDBC Connection [{}]", this.connection);
            this.connection.commit();
        }
    }

    @Override
    public void rollback() {
        if (this.connection != null && !this.isConnectionTransactional && !this.autoCommit) {
            try {
                logger.debug("Rolling back JDBC Connection [{}]", this.connection);
                this.connection.rollback();
            } catch (SQLException e) {
                logger.warn("Failed to rollback connection", e);
            }
        }
    }

    @Override
    public void close() {
        if (this.connection != null) {
            DataSourceUtils.releaseConnection(this.connection, this.dataSource);
            logger.debug("Released JDBC Connection [{}]", this.connection);
        }
    }

    /**
     * 获取数据源
     *
     * @return 数据源
     */
    public DataSource getDataSource() {
        return dataSource;
    }

    /**
     * 检查连接是否由Spring事务管理
     *
     * @return 是否由Spring事务管理
     */
    public boolean isConnectionTransactional() {
        return isConnectionTransactional;
    }
} 