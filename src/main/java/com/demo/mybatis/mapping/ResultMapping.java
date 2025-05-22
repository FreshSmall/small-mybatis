package com.demo.mybatis.mapping;

import com.demo.mybatis.type.JdbcType;

/**
 * 结果映射
 * 
 * @author yinchao
 * @date 2025/5/23
 */
public class ResultMapping {
    
    private String property;
    private String column;
    private Class<?> javaType;
    private JdbcType jdbcType;
    
    private ResultMapping() {
        // 私有构造函数，通过 Builder 创建
    }
    
    public String getProperty() {
        return property;
    }
    
    public String getColumn() {
        return column;
    }
    
    public Class<?> getJavaType() {
        return javaType;
    }
    
    public JdbcType getJdbcType() {
        return jdbcType;
    }
    
    /**
     * 建造者
     */
    public static class Builder {
        private ResultMapping resultMapping = new ResultMapping();
        
        public Builder(String property, String column) {
            resultMapping.property = property;
            resultMapping.column = column;
        }
        
        public Builder javaType(Class<?> javaType) {
            resultMapping.javaType = javaType;
            return this;
        }
        
        public Builder jdbcType(JdbcType jdbcType) {
            resultMapping.jdbcType = jdbcType;
            return this;
        }
        
        public ResultMapping build() {
            return resultMapping;
        }
    }
}
