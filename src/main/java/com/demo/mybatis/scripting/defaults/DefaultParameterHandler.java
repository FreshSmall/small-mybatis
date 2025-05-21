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
                Object value;
                if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
                    value = parameterObject;
                } else {
                    // 通过 MetaObject.getValue 反射取得值设进去
                    MetaObject metaObject = configuration.newMetaObject(parameterObject);
                    value = metaObject.getValue(propertyName);
                }
                JdbcType jdbcType = parameterMapping.getJdbcType();

                // 设置参数
                System.out.println("根据每个ParameterMapping中的TypeHandler设置对应的参数信息 value：" + value);
                TypeHandler typeHandler = parameterMapping.getTypeHandler();
                if (typeHandler == null) {
                    throw new RuntimeException("没有找到对应的TypeHandler，property:" + propertyName + " javaType:" + parameterMapping.getJavaType());
                }
                typeHandler.setParameter(ps, i + 1, value, jdbcType);
            }
        }
    }
}
