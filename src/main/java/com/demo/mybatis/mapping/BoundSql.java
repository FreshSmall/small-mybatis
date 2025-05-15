package com.demo.mybatis.mapping;

import java.util.Map;

public class BoundSql {

    private String sql;
    private Map<Integer, String> parameterMappings;
    private String resultType;
    private String parameterType;

    public BoundSql(String sql, Map<Integer, String> parameterMappings, String resultType, String parameterType) {
        this.sql = sql;
        this.parameterMappings = parameterMappings;
        this.resultType = resultType;
        this.parameterType = parameterType;
    }

    public String getSql() {
        return sql;
    }

    public Map<Integer, String> getParameterMappings() {
        return parameterMappings;
    }

    public String getResultType() {
        return resultType;
    }

    public String getParameterType() {
        return parameterType;
    }
}
