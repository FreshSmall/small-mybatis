package com.demo.mybatis.mapping;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * 结果映射
 * 
 * @author yinchao
 * @date 2025/5/23
 */
public class ResultMap {
    
    private String id;
    private Class<?> type;
    private List<ResultMapping> resultMappings;
    private Set<String> mappedColumns;
    
    private ResultMap() {
        // 私有构造函数，通过 Builder 创建
    }
    
    public String getId() {
        return id;
    }
    
    public Class<?> getType() {
        return type;
    }
    
    public List<ResultMapping> getResultMappings() {
        return resultMappings;
    }
    
    public Set<String> getMappedColumns() {
        return mappedColumns;
    }
    
    /**
     * 建造者
     */
    public static class Builder {
        private ResultMap resultMap = new ResultMap();
        
        public Builder(String id, Class<?> type, List<ResultMapping> resultMappings) {
            resultMap.id = id;
            resultMap.type = type;
            resultMap.resultMappings = resultMappings;
            resultMap.mappedColumns = new HashSet<>();
            for (ResultMapping resultMapping : resultMappings) {
                resultMap.mappedColumns.add(resultMapping.getColumn().toUpperCase(Locale.ENGLISH));
            }
        }
        
        public ResultMap build() {
            return resultMap;
        }
    }
}
