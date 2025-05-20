package com.demo.mybatis.builder;

/*
 * @Author: yinchao
 * @Date: 2025-05-19 22:45:37
 * @LastEditors: yinchao
 * @LastEditTime: 2025-05-20 22:51:32
 * @Description: 
 */
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.demo.mybatis.mapping.ParameterMapping;
import com.demo.mybatis.mapping.SqlSource;
import com.demo.mybatis.parsing.GenericTokenParser;
import com.demo.mybatis.parsing.TokenHandler;
import com.demo.mybatis.reflection.MetaClass;
import com.demo.mybatis.reflection.MetaObject;
import com.demo.mybatis.session.Configuration;

/*
 * @Author: yinchao
 * @Date: 2025-05-19 22:45:37
 * @LastEditors: yinchao
 * @LastEditTime: 2025-05-19 22:45:38
 * @Description: 
 */
public class SqlSourceBuilder extends BaseBuilder {

    public SqlSourceBuilder(Configuration configuration) {
        super(configuration);
    }

    public SqlSource parse(String originalSql, Class<?> parameterType, Map<String, Object> additionalParameters) {
        ParameterMappingTokenHandler handler = new ParameterMappingTokenHandler(configuration, parameterType,
                additionalParameters);
        GenericTokenParser parser = new GenericTokenParser("#{", "}", handler);
        String sql = parser.parse(originalSql);
        // 返回静态 SQL
        return new StaticSqlSource(configuration, sql, handler.getParameterMappings());
    }

    private static class ParameterMappingTokenHandler extends BaseBuilder implements TokenHandler {

        private List<ParameterMapping> parameterMappings = new ArrayList<>();
        private Class<?> parameterType;
        private MetaObject metaParameters;

        public ParameterMappingTokenHandler(Configuration configuration, Class<?> parameterType,
                Map<String, Object> additionalParameters) {
            super(configuration);
            this.parameterType = parameterType;
            this.metaParameters = configuration.newMetaObject(additionalParameters);
        }

        public List<ParameterMapping> getParameterMappings() {
            return parameterMappings;
        }

        @Override
        public String handleToken(String content) {
            parameterMappings.add(buildParameterMapping(content));
            return "?";
        }

        // 构建参数映射
        private ParameterMapping buildParameterMapping(String content) {

            // 先解析参数映射,就是转化成一个 HashMap | #{favouriteSection,jdbcType=VARCHAR}
            Map<String, String> propertiesMap = new ParameterExpression(content);
            String property = propertiesMap.get("property");
            Class<?> propertyType;
            if (typeHandlerRegistry.hasTypeHandler(parameterType)) {
                propertyType = parameterType;
            } else if (property != null) {
                MetaClass metaClass = MetaClass.forClass(parameterType);
                if (metaClass.hasGetter(property)) {
                    propertyType = metaClass.getGetterType(property);
                } else {
                    propertyType = Object.class;
                }
            } else {
                propertyType = Object.class;
            }
            System.out.println("构建参数映射 property：" + property + " propertyType：" + propertyType);
            ParameterMapping.Builder builder = new ParameterMapping.Builder(configuration, property, propertyType);
            return builder.build();
        }

    }

}
