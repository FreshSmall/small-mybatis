package com.demo.mybatis.session;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.demo.mybatis.binding.MapperRegistry;
import com.demo.mybatis.datasource.druid.DruidDataSourceFactory;
import com.demo.mybatis.datasource.pooled.PooledDataSourceFactory;
import com.demo.mybatis.datasource.unpooled.UnpooledDataSourceFactory;
import com.demo.mybatis.executor.Executor;
import com.demo.mybatis.executor.SimpleExecutor;
import com.demo.mybatis.executor.resultset.DefaultResultSetHandler;
import com.demo.mybatis.executor.resultset.ResultSetHandler;
import com.demo.mybatis.executor.statement.PreparedStatementHandler;
import com.demo.mybatis.executor.statement.StatementHandler;
import com.demo.mybatis.mapping.BoundSql;
import com.demo.mybatis.mapping.Environment;
import com.demo.mybatis.mapping.MappedStatement;
import com.demo.mybatis.reflection.MetaObject;
import com.demo.mybatis.reflection.factory.DefaultObjectFactory;
import com.demo.mybatis.reflection.factory.ObjectFactory;
import com.demo.mybatis.reflection.wrapper.DefaultObjectWrapperFactory;
import com.demo.mybatis.reflection.wrapper.ObjectWrapperFactory;
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

    public Configuration() {
        typeAliasRegistry.registerAlias("JDBC", JdbcTransactionFactory.class);
        typeAliasRegistry.registerAlias("DRUID", DruidDataSourceFactory.class);
        typeAliasRegistry.registerAlias("POOLED", PooledDataSourceFactory.class);
        typeAliasRegistry.registerAlias("UNPOOLED", UnpooledDataSourceFactory.class);
        languageRegistry.setDefaultDriverClass(XMLLanguageDriver.class);
    }

    public void addMappers(String packageName) {
        mapperRegistry.addMappers(packageName);
    }

    public <T> void addMapper(Class<T> type) {
        mapperRegistry.addMapper(type);
    }

    public <T> T getMapper(Class<T> type, SqlSession sqlSession) {
        return mapperRegistry.getMapper(type, sqlSession);
    }

    public boolean hasMapper(Class<?> type) {
        return mapperRegistry.hasMapper(type);
    }

    public void addMappedStatement(MappedStatement ms) {
        mappedStatements.put(ms.getId(), ms);
    }

    public MappedStatement getMappedStatement(String id) {
        return mappedStatements.get(id);
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
        return new DefaultResultSetHandler(executor, mappedStatement, boundSql);
    }

    /**
     * 生产执行器
     */
    public Executor newExecutor(Transaction transaction) {
        return new SimpleExecutor(this, transaction);
    }

    /**
     * 创建语句处理器
     */
    public StatementHandler newStatementHandler(Executor executor, MappedStatement mappedStatement, Object parameter, ResultHandler resultHandler, BoundSql boundSql) {
        return new PreparedStatementHandler(executor, mappedStatement, parameter, resultHandler, boundSql);
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

    

}
