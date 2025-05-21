package com.demo.mybatis.scripting.defaults;

/*
 * @Author: yinchao
 * @Date: 2025-05-19 22:48:57
 * @LastEditors: yinchao
 * @LastEditTime: 2025-05-20 22:55:44
 * @Description:
 */
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import com.demo.mybatis.executor.parameter.ParameterHandler;
import com.demo.mybatis.mapping.BoundSql;
import com.demo.mybatis.mapping.MappedStatement;
import com.demo.mybatis.mapping.ParameterMapping;
import com.demo.mybatis.reflection.MetaObject;
import com.demo.mybatis.session.Configuration;
import com.demo.mybatis.type.JdbcType;
import com.demo.mybatis.type.TypeHandler;
import com.demo.mybatis.type.TypeHandlerRegistry;

/*
 * @Author: yinchao
 * @Date: 2025-05-19 22:48:00
 * @LastEditors: yinchao
 * @LastEditTime: 2025-05-20 22:54:26
 * @Description:
 */
public class DefaultParameterHandler implements ParameterHandler {

    private final TypeHandlerRegistry typeHandlerRegistry;

    private final MappedStatement mappedStatement;
    private final Object parameterObject;
    private BoundSql boundSql;
    private Configuration configuration;

    public DefaultParameterHandler(MappedStatement mappedStatement, Object parameterObject, BoundSql boundSql) {
        this.mappedStatement = mappedStatement;
        this.configuration = mappedStatement.getConfiguration();
        this.typeHandlerRegistry = mappedStatement.getConfiguration().getTypeHandlerRegistry();
        this.parameterObject = parameterObject;
        this.boundSql = boundSql;
    }

    @Override
    public Object getParameterObject() {
        // TODO Auto-generated method stub
        return parameterObject;
    }

    @Override
    public void setParameters(PreparedStatement ps) throws SQLException {
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        if (null != parameterMappings) {
            for (int i = 0; i < parameterMappings.size(); i++) {
                ParameterMapping parameterMapping = parameterMappings.get(i);
                String propertyName = parameterMapping.getProperty();
                Object value = null;
                if (parameterObject == null) {
                    // 如果参数对象为null，直接使用null值
                    value = null;
                } else if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass()) && !propertyName.contains(".")) {
                    // 如果参数是基本类型或者有对应的TypeHandler，并且属性名不包含点号（不是嵌套属性）
                    // 但是要排除复杂对象
                    if (parameterObject instanceof String ||
                        parameterObject instanceof Integer ||
                        parameterObject instanceof Long ||
                        parameterObject instanceof Double ||
                        parameterObject instanceof Float ||
                        parameterObject instanceof Boolean ||
                        parameterObject instanceof Byte ||
                        parameterObject instanceof Short) {
                        value = parameterObject;
                    } else {
                        // 复杂对象，通过反射获取属性值
                        MetaObject metaObject = configuration.newMetaObject(parameterObject);
                        value = metaObject.getValue(propertyName);
                    }
                } else {
                    // 通过 MetaObject.getValue 反射取得值设进去
                    MetaObject metaObject = configuration.newMetaObject(parameterObject);
                    value = metaObject.getValue(propertyName);
                }

                // 设置参数
                System.out.println("根据每个ParameterMapping中的TypeHandler设置对应的参数信息 value：" + value);

                // 根据值的类型选择合适的TypeHandler
                TypeHandler typeHandler;
                if (value == null) {
                    JdbcType jdbcType = parameterMapping.getJdbcType();
                    typeHandler = parameterMapping.getTypeHandler();
                    if (typeHandler == null) {
                        typeHandler = typeHandlerRegistry.getTypeHandler(Object.class, jdbcType);
                    }
                    typeHandler.setParameter(ps, i + 1, null, jdbcType);
                } else {
                    // 根据值的类型获取TypeHandler
                    typeHandler = typeHandlerRegistry.getTypeHandler(value.getClass());
                    if (typeHandler == null) {
                        // 如果没有找到对应的TypeHandler，使用基本类型的TypeHandler
                        if (value instanceof Long) {
                            typeHandler = typeHandlerRegistry.getTypeHandler(Long.class);
                        } else if (value instanceof Integer) {
                            typeHandler = typeHandlerRegistry.getTypeHandler(Integer.class);
                        } else if (value instanceof String) {
                            typeHandler = typeHandlerRegistry.getTypeHandler(String.class);
                        } else {
                            // 默认使用Object类型的TypeHandler
                            typeHandler = typeHandlerRegistry.getTypeHandler(Object.class);
                        }
                    }
                    if (typeHandler == null) {
                        throw new RuntimeException("没有找到对应的TypeHandler，property:" + propertyName + " javaType:" + value.getClass().getName());
                    }
                    typeHandler.setParameter(ps, i + 1, value, null);
                }
            }
        }
    }
}
