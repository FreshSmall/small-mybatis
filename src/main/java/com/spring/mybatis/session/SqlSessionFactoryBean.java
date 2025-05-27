package com.spring.mybatis.session;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;

import com.demo.mybatis.session.Configuration;
import com.demo.mybatis.session.SqlSessionFactory;
import com.demo.mybatis.session.SqlSessionFactoryBuilder;

/**
 * @author: yinchao
 * @ClassName: SqlSessionFactoryBean
 * @Description: Spring工厂Bean，用于创建和配置SqlSessionFactory
 * 实现FactoryBean接口，集成到Spring容器生命周期管理中
 * @team wuhan operational dev.
 * @date: 2025/5/6 10:00
 */
public class SqlSessionFactoryBean implements FactoryBean<SqlSessionFactory>, InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(SqlSessionFactoryBean.class);

    /**
     * MyBatis配置文件位置
     */
    private Resource configLocation;

    /**
     * 数据源
     */
    private DataSource dataSource;

    /**
     * MyBatis配置对象
     */
    private Configuration configuration;

    /**
     * SqlSessionFactory实例
     */
    private SqlSessionFactory sqlSessionFactory;

    /**
     * 是否已初始化
     */
    private boolean initialized = false;

    @Override
    public SqlSessionFactory getObject() throws Exception {
        if (this.sqlSessionFactory == null) {
            afterPropertiesSet();
        }
        return this.sqlSessionFactory;
    }

    @Override
    public Class<?> getObjectType() {
        return this.sqlSessionFactory != null ? this.sqlSessionFactory.getClass() : SqlSessionFactory.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (!initialized) {
            this.sqlSessionFactory = buildSqlSessionFactory();
            initialized = true;
        }
    }

    /**
     * 构建SqlSessionFactory
     *
     * @return SqlSessionFactory实例
     * @throws Exception 构建异常
     */
    protected SqlSessionFactory buildSqlSessionFactory() throws Exception {
        SqlSessionFactoryBuilder builder = new SqlSessionFactoryBuilder();

        if (this.configLocation != null) {
            // 从配置文件构建
            try (InputStream inputStream = this.configLocation.getInputStream();
                 Reader reader = new InputStreamReader(inputStream)) {
                return builder.build(reader);
            } catch (IOException e) {
                throw new RuntimeException("Failed to parse config resource: " + this.configLocation, e);
            }
        } else {
            // 使用默认配置或自定义配置
            Configuration config = this.configuration != null ? this.configuration : new Configuration();
            
            // 如果提供了数据源，则设置到环境中
            if (this.dataSource != null) {
                // 创建环境配置
                com.demo.mybatis.transaction.TransactionFactory transactionFactory = 
                    new com.demo.mybatis.transaction.jdbc.JdbcTransactionFactory();
                
                com.demo.mybatis.mapping.Environment environment = 
                    new com.demo.mybatis.mapping.Environment.Builder("development")
                        .transactionFactory(transactionFactory)
                        .dataSource(this.dataSource)
                        .build();
                
                config.setEnvironment(environment);
                logger.info("Using provided DataSource for SqlSessionFactory with environment: {}", environment.getId());
            } else {
                logger.warn("No DataSource provided for SqlSessionFactory");
            }

            // 加载XML Mapper文件
            try {
                loadMapperXmlFiles(config);
            } catch (Exception e) {
                logger.warn("Failed to load XML mapper files: {}", e.getMessage());
            }

            return builder.build(config);
        }
    }
    
    /**
     * 加载XML Mapper文件
     *
     * @param config Configuration实例
     * @throws Exception 加载异常
     */
    private void loadMapperXmlFiles(Configuration config) throws Exception {
        // 加载测试用的XML文件
        String[] mapperFiles = {
            "mapper/user_mapper.xml",
            "mapper/dynamic_sql_mapper.xml",
            "mapper/cache_test_mapper.xml"
        };
        
        for (String mapperFile : mapperFiles) {
            try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(mapperFile)) {
                if (inputStream != null) {
                    com.demo.mybatis.builder.xml.XMLMapperBuilder mapperBuilder = 
                        new com.demo.mybatis.builder.xml.XMLMapperBuilder(inputStream, config, mapperFile);
                    mapperBuilder.parse();
                    logger.debug("Loaded XML mapper file: {}", mapperFile);
                } else {
                    logger.warn("XML mapper file not found: {}", mapperFile);
                }
            }
        }
    }

    /**
     * 设置MyBatis配置文件位置
     *
     * @param configLocation 配置文件位置
     */
    public void setConfigLocation(Resource configLocation) {
        this.configLocation = configLocation;
    }

    /**
     * 设置数据源
     *
     * @param dataSource 数据源
     */
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * 设置MyBatis配置对象
     *
     * @param configuration 配置对象
     */
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    /**
     * 获取配置文件位置
     *
     * @return 配置文件位置
     */
    public Resource getConfigLocation() {
        return configLocation;
    }

    /**
     * 获取数据源
     *
     * @return 数据源
     */
    public DataSource getDataSource() {
        return dataSource;
    }

    /**
     * 获取配置对象
     *
     * @return 配置对象
     */
    public Configuration getConfiguration() {
        return configuration;
    }
} 