package com.demo.mybatis.executor.keygen;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.demo.mybatis.mapping.MappedStatement;
import com.demo.mybatis.reflection.MetaObject;

/**
 * JDBC3 方式的主键生成器实现
 * 使用 Statement.getGeneratedKeys() 获取数据库生成的主键
 */
public class Jdbc3KeyGenerator implements KeyGenerator {
    
    @Override
    public void processGeneratedKeys(Statement statement, Object parameter, MappedStatement mappedStatement) {
        if (parameter != null && mappedStatement.isUseGeneratedKeys()) {
            try {
                ResultSet rs = statement.getGeneratedKeys();
                if (rs.next()) {
                    String keyProperty = mappedStatement.getKeyProperty();
                    if (keyProperty != null && !keyProperty.isEmpty()) {
                        MetaObject metaParam = mappedStatement.getConfiguration().newMetaObject(parameter);
                        if (metaParam.hasSetter(keyProperty)) {
                            Class<?> keyType = metaParam.getSetterType(keyProperty);
                            // 根据属性类型获取对应的值
                            Object value = getValueByType(rs, keyType);
                            metaParam.setValue(keyProperty, value);
                        }
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException("Error getting generated keys.", e);
            }
        }
    }
    
    private Object getValueByType(ResultSet rs, Class<?> type) throws SQLException {
        if (type == Integer.class || type == int.class) {
            return rs.getInt(1);
        } else if (type == Long.class || type == long.class) {
            return rs.getLong(1);
        } else if (type == String.class) {
            return rs.getString(1);
        } else {
            return rs.getObject(1);
        }
    }
}
