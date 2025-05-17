package com.demo.mybatis.datasource.unpooled;

import java.lang.reflect.Method;
import java.util.Properties;

import javax.sql.DataSource;

import com.demo.mybatis.datasource.DataSourceFactory;
import com.demo.mybatis.datasource.pooled.PooledDataSource;
import com.demo.mybatis.reflection.MetaObject;

import cn.hutool.core.util.ReflectUtil;
import com.demo.mybatis.reflection.SystemMetaObject;

/**
 * @author: yinchao
 * @ClassName: UnpooledDataSourceFactory
 * @Description:
 * @team wuhan operational dev.
 * @date: 2025/5/15 22:06
 */
public class UnpooledDataSourceFactory implements DataSourceFactory {

    protected DataSource dataSource;

    public UnpooledDataSourceFactory() {
        this.dataSource = new UnpooledDataSource();
    }

    @Override
    public void setProperties(Properties props) {
        MetaObject metaObject = SystemMetaObject.forObject(dataSource);
        for (Object key : props.keySet()) {
            String propertyName = (String) key;
            if (metaObject.hasSetter(propertyName)) {
                String value = (String) props.get(propertyName);
                Object convertedValue = convertValue(metaObject, propertyName, value);
                metaObject.setValue(propertyName, convertedValue);
            }
        }
    }

    public void customSetProperties(Properties props) {
        DataSource dataSource1 = this.getDataSource();
        for (Object key : props.keySet()) {
            String propertyName = (String) key;
            String methodName = "set" + Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1);
            Method method = ReflectUtil.getMethodByName(dataSource1.getClass(), methodName);
            ReflectUtil.invoke(dataSource1, method, props.get(propertyName));
        }
    }

    @Override
    public DataSource getDataSource() {
        return this.dataSource;
    }

    /**
     * 根据setter的类型,将配置文件中的值强转成相应的类型
     */
    private Object convertValue(MetaObject metaObject, String propertyName, String value) {
        Object convertedValue = value;
        Class<?> targetType = metaObject.getSetterType(propertyName);
        if (targetType == Integer.class || targetType == int.class) {
            convertedValue = Integer.valueOf(value);
        } else if (targetType == Long.class || targetType == long.class) {
            convertedValue = Long.valueOf(value);
        } else if (targetType == Boolean.class || targetType == boolean.class) {
            convertedValue = Boolean.valueOf(value);
        }
        return convertedValue;
    }
}
