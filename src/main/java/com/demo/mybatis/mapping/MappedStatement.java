package com.demo.mybatis.mapping;

import java.util.Map;

import com.demo.mybatis.cache.Cache;
import com.demo.mybatis.scripting.LanguageDriver;
import com.demo.mybatis.session.Configuration;

/**
 * @author: yinchao
 * @ClassName: MappedStatement
 * @Description:
 * @team wuhan operational dev.
 * @date: 2025/5/6 22:45
 */
public class MappedStatement {

    private Configuration configuration;
    private String id;
    private SqlCommandType sqlCommandType;
    private SqlSource sqlSource;
    Class<?> resultType;
    private LanguageDriver lang;
    private String resultMap; // 新增 resultMap 属性

    // 新增自增主键相关属性
    private boolean useGeneratedKeys;
    private String keyProperty;

    // 新增：是否启用二级缓存
    private boolean cacheEnabled = false;

    // 新增：缓存配置
    private Cache cache;


    MappedStatement() {
        // constructor disabled
    }

    public LanguageDriver getLang() {
        return lang;
    }

    /**
     * 建造者
     */
    public static class Builder {

        private MappedStatement mappedStatement = new MappedStatement();

        public Builder(Configuration configuration, String id, SqlCommandType sqlCommandType, SqlSource sqlSource, Class<?> resultType) {
            mappedStatement.configuration = configuration;
            mappedStatement.id = id;
            mappedStatement.sqlCommandType = sqlCommandType;
            mappedStatement.sqlSource = sqlSource;
            mappedStatement.resultType = resultType;
            mappedStatement.lang = configuration.getDefaultScriptingLanguageInstance();
        }

        public Builder resultMap(String resultMap) {
            mappedStatement.resultMap = resultMap;
            return this;
        }

        // 新增方法：设置是否使用自增主键
        public Builder useGeneratedKeys(boolean useGeneratedKeys) {
            mappedStatement.useGeneratedKeys = useGeneratedKeys;
            return this;
        }

        // 新增方法：设置自增主键对应的属性名
        public Builder keyProperty(String keyProperty) {
            mappedStatement.keyProperty = keyProperty;
            return this;
        }

        // 新增方法：设置是否启用二级缓存
        public Builder cacheEnabled(boolean cacheEnabled) {
            mappedStatement.cacheEnabled = cacheEnabled;
            return this;
        }

        // 新增方法：设置缓存
        public Builder cache(Cache cache) {
            mappedStatement.cache = cache;
            return this;
        }

        public MappedStatement build() {
            assert mappedStatement.configuration != null;
            assert mappedStatement.id != null;
            return mappedStatement;
        }

    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public String getId() {
        return id;
    }

    public SqlCommandType getSqlCommandType() {
        return sqlCommandType;
    }

    public SqlSource getSqlSource() {
        return sqlSource;
    }

    public Class<?> getResultType() {
        return resultType;
    }

    public String getResultMap() {
        return resultMap;
    }

    // 新增方法：获取是否使用自增主键
    public boolean isUseGeneratedKeys() {
        return useGeneratedKeys;
    }

    // 新增方法：获取自增主键对应的属性名
    public String getKeyProperty() {
        return keyProperty;
    }

    // 新增方法：获取是否启用二级缓存
    public boolean isCacheEnabled() {
        return cacheEnabled;
    }

    // 新增方法：获取缓存
    public Cache getCache() {
        return cache;
    }
}
