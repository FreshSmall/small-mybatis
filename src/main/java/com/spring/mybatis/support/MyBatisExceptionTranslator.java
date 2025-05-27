package com.spring.mybatis.support;

import java.sql.SQLException;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.support.PersistenceExceptionTranslator;
import org.springframework.jdbc.support.SQLExceptionTranslator;
import org.springframework.jdbc.support.SQLStateSQLExceptionTranslator;

/**
 * @author: yinchao
 * @ClassName: MyBatisExceptionTranslator
 * @Description: MyBatis异常转换器，将MyBatis异常转换为Spring的DataAccessException
 * @team wuhan operational dev.
 * @date: 2025/5/6 11:00
 */
public class MyBatisExceptionTranslator implements PersistenceExceptionTranslator {

    private final SQLExceptionTranslator sqlExceptionTranslator;

    /**
     * 默认构造函数，使用SQLStateSQLExceptionTranslator
     */
    public MyBatisExceptionTranslator() {
        this.sqlExceptionTranslator = new SQLStateSQLExceptionTranslator();
    }

    /**
     * 构造函数
     *
     * @param sqlExceptionTranslator SQL异常转换器
     */
    public MyBatisExceptionTranslator(SQLExceptionTranslator sqlExceptionTranslator) {
        this.sqlExceptionTranslator = sqlExceptionTranslator;
    }

    @Override
    public DataAccessException translateExceptionIfPossible(RuntimeException ex) {
        // 检查是否是已经转换过的Spring异常
        if (ex instanceof DataAccessException) {
            return (DataAccessException) ex;
        }

        // 查找SQLException原因
        Throwable cause = ex;
        while (cause != null) {
            if (cause instanceof SQLException) {
                SQLException sqlEx = (SQLException) cause;
                DataAccessException translated = sqlExceptionTranslator.translate(
                    "MyBatis operation", null, sqlEx);
                if (translated != null) {
                    return translated;
                }
                break;
            }
            cause = cause.getCause();
        }

        // 如果找不到SQLException，返回通用的数据访问异常
        return new DataAccessResourceFailureException("MyBatis operation failed", ex);
    }
} 