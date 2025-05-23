package com.demo.mybatis.session;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.demo.mybatis.binding.MapperRegistry;
import com.demo.mybatis.cache.CacheManager;
import com.demo.mybatis.datasource.druid.DruidDataSourceFactory;
import com.demo.mybatis.datasource.pooled.PooledDataSourceFactory;
import com.demo.mybatis.datasource.unpooled.UnpooledDataSourceFactory;
import com.demo.mybatis.executor.CachingExecutor;
import com.demo.mybatis.executor.Executor;
import com.demo.mybatis.executor.SimpleExecutor;
import com.demo.mybatis.executor.parameter.ParameterHandler;
import com.demo.mybatis.executor.resultset.DefaultResultSetHandler;
import com.demo.mybatis.executor.resultset.ResultSetHandler;
import com.demo.mybatis.executor.statement.PreparedStatementHandler;
import com.demo.mybatis.executor.statement.StatementHandler;
import com.demo.mybatis.mapping.BoundSql;
import com.demo.mybatis.mapping.Environment;
import com.demo.mybatis.mapping.MappedStatement;
import com.demo.mybatis.mapping.ResultMap;
import com.demo.mybatis.plugin.Interceptor;
import com.demo.mybatis.plugin.InterceptorChain;
import com.demo.mybatis.reflection.MetaObject;
import com.demo.mybatis.reflection.factory.DefaultObjectFactory;
import com.demo.mybatis.reflection.factory.ObjectFactory;
import com.demo.mybatis.reflection.wrapper.DefaultObjectWrapperFactory;
import com.demo.mybatis.reflection.wrapper.ObjectWrapperFactory;
import com.demo.mybatis.scripting.AnnotationLanguageDriver;
import com.demo.mybatis.scripting.LanguageDriver;
import com.demo.mybatis.scripting.LanguageDriverRegistry;
import com.demo.mybatis.scripting.xmltags.XMLLanguageDriver;
import com.demo.mybatis.transaction.Transaction;
import com.demo.mybatis.transaction.jdbc.JdbcTransactionFactory;
import com.demo.mybatis.type.TypeAliasRegistry;
import com.demo.mybatis.type.TypeHandlerRegistry;

/**
 * @author: yinchao
 * @ClassName: Configuration
 * @Description:
 * @team wuhan operational dev.
 * @date: 2025/5/6 22:45
 */
public class Configuration {

    //环境
    protected Environment environment;

    // 映射注册机
    protected MapperRegistry mapperRegistry = new MapperRegistry(this);

    // 映射的语句，存在Map里
    protected final Map<String, MappedStatement> mappedStatements = new HashMap<>();

    // 结果映射，存在Map里
    protected final Map<String, ResultMap> resultMaps = new HashMap<>();

    // 类型别名注册机
    protected final TypeAliasRegistry typeAliasRegistry = new TypeAliasRegistry();
    protected final LanguageDriverRegistry languageRegistry = new LanguageDriverRegistry();

    // 类型处理器注册机
    protected final TypeHandlerRegistry typeHandlerRegistry = new TypeHandlerRegistry();

    // 对象工厂和对象包装器工厂
    protected ObjectFactory objectFactory = new DefaultObjectFactory();
    protected ObjectWrapperFactory objectWrapperFactory = new DefaultObjectWrapperFactory();

    protected final Set<String> loadedResources = new HashSet<>();

    protected String databaseId;

    // 插件拦截器链
    protected final InterceptorChain interceptorChain = new InterceptorChain();

    // 新增：缓存管理器
    protected final CacheManager cacheManager = new CacheManager();

    // 新增：全局二级缓存开关
    protected boolean cacheEnabled = true;

    public Configuration() {
        typeAliasRegistry.registerAlias("JDBC", JdbcTransactionFactory.class);
        typeAliasRegistry.registerAlias("DRUID", DruidDataSourceFactory.class);
        typeAliasRegistry.registerAlias("POOLED", PooledDataSourceFactory.class);
        typeAliasRegistry.registerAlias("UNPOOLED", UnpooledDataSourceFactory.class);

        // 注册 XML 语言驱动器
        languageRegistry.setDefaultDriverClass(XMLLanguageDriver.class);

        // 注册注解语言驱动器
        languageRegistry.register(AnnotationLanguageDriver.class);
    }

    public void addMappers(String packageName) {
        mapperRegistry.addMappers(packageName);
    }

    public <T> void addMapper(Class<T> type) {
        mapperRegistry.addMapper(type);
    }

    public <T> boolean hasMapper(Class<T> type) {
        return mapperRegistry.hasMapper(type);
    }

    public <T> T getMapper(Class<T> type, SqlSession sqlSession) {
        return mapperRegistry.getMapper(type, sqlSession);
    }

    public void addMappedStatement(MappedStatement ms) {
        mappedStatements.put(ms.getId(), ms);
    }

    public MappedStatement getMappedStatement(String id) {
        return mappedStatements.get(id);
    }

    public boolean hasStatement(String statementName) {
        return mappedStatements.containsKey(statementName);
    }

    public void addResultMap(ResultMap resultMap) {
        resultMaps.put(resultMap.getId(), resultMap);
    }

    public ResultMap getResultMap(String id) {
        return resultMaps.get(id);
    }

    public boolean hasResultMap(String id) {
        return resultMaps.containsKey(id);
    }

    public TypeAliasRegistry getTypeAliasRegistry() {
        return typeAliasRegistry;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }


    public TypeHandlerRegistry getTypeHandlerRegistry() {
        return typeHandlerRegistry;
    }

    /**
     * 创建结果集处理器
     */
    public ResultSetHandler newResultSetHandler(Executor executor, MappedStatement mappedStatement, BoundSql boundSql) {
        ResultSetHandler resultSetHandler = new DefaultResultSetHandler(executor, mappedStatement, boundSql);
        // 应用插件
        return (ResultSetHandler) interceptorChain.pluginAll(resultSetHandler);
    }

    /**
     * 生产执行器
     */
    public Executor newExecutor(Transaction transaction) {
        Executor executor = new SimpleExecutor(this, transaction);

        // 如果启用了全局二级缓存，使用CachingExecutor装饰基础执行器
        if (cacheEnabled) {
            executor = new CachingExecutor(executor);
        }

        // 应用插件
        return (Executor) interceptorChain.pluginAll(executor);
    }

    /**
     * 创建语句处理器
     */
    public StatementHandler newStatementHandler(Executor executor, MappedStatement mappedStatement, Object parameter, ResultHandler resultHandler, BoundSql boundSql) {
        StatementHandler statementHandler = new PreparedStatementHandler(executor, mappedStatement, parameter, resultHandler, boundSql);
        // 应用插件
        return (StatementHandler) interceptorChain.pluginAll(statementHandler);
    }

    public MetaObject newMetaObject(Object parameterObject) {
        return MetaObject.forObject(parameterObject, objectFactory, objectWrapperFactory);
    }


    public boolean isResourceLoaded(String resource) {
        return loadedResources.contains(resource);
    }

    public void addLoadedResource(String resource) {
        loadedResources.add(resource);
    }

    public LanguageDriverRegistry getLanguageRegistry() {
        return languageRegistry;
    }

    public String getDatabaseId() {
        return databaseId;
    }

    /**
     * 添加拦截器
     */
    public void addInterceptor(Interceptor interceptor) {
        interceptorChain.addInterceptor(interceptor);
    }

    /**
     * 获取拦截器链
     */
    public InterceptorChain getInterceptorChain() {
        return interceptorChain;
    }

    /**
     * 获取缓存管理器
     */
    public CacheManager getCacheManager() {
        return cacheManager;
    }

    /**
     * 判断是否启用全局二级缓存
     */
    public boolean isCacheEnabled() {
        return cacheEnabled;
    }

    /**
     * 设置是否启用全局二级缓存
     */
    public void setCacheEnabled(boolean cacheEnabled) {
        this.cacheEnabled = cacheEnabled;
    }

    public ParameterHandler newParameterHandler(MappedStatement mappedStatement, Object parameterObject, BoundSql boundSql) {
        // 创建参数处理器
        ParameterHandler parameterHandler = mappedStatement.getLang().createParameterHandler(mappedStatement, parameterObject, boundSql);
        // 应用插件
        return (ParameterHandler) interceptorChain.pluginAll(parameterHandler);
    }

    public LanguageDriver getDefaultScriptingLanguageInstance() {
        return languageRegistry.getDefaultDriver();
    }
}
