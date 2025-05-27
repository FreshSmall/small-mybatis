# Small-MyBatis Spring 整合使用指南

## 概述

本文档介绍如何使用 small-mybatis 与 Spring 框架的整合功能。通过整合，可以实现：

- MyBatis 参与 Spring 事务管理
- 自动创建和注入 Mapper 接口
- 异常转换为 Spring 的 DataAccessException
- 应用代码不依赖于 MyBatis 具体实现

## 核心组件

### 1. SqlSessionFactoryBean
Spring 工厂 Bean，用于创建 SqlSessionFactory。

### 2. SqlSessionTemplate
线程安全的 SqlSession 实现，集成 Spring 事务管理。

### 3. MapperScannerConfigurer
自动扫描指定包下的 Mapper 接口并注册为 Spring Bean。

### 4. MapperFactoryBean
为每个 Mapper 接口创建代理对象。

### 5. SpringManagedTransaction
集成 Spring 事务管理的 Transaction 实现。

## 使用方式

### 1. XML 配置方式

```xml
<!-- 配置数据源 -->
<bean id="dataSource" class="com.alibaba.druid.pool.DruidDataSource">
    <property name="url" value="jdbc:mysql://localhost:3306/mybatis"/>
    <property name="username" value="root"/>
    <property name="password" value="123456"/>
    <property name="driverClassName" value="com.mysql.jdbc.Driver"/>
</bean>

<!-- 配置 SqlSessionFactory -->
<bean id="sqlSessionFactory" class="com.spring.mybatis.session.SqlSessionFactoryBean">
    <property name="dataSource" ref="dataSource"/>
    <property name="configLocation" value="classpath:mybatis-config.xml"/>
</bean>

<!-- 配置 SqlSessionTemplate -->
<bean id="sqlSessionTemplate" class="com.spring.mybatis.session.SqlSessionTemplate">
    <constructor-arg ref="sqlSessionFactory"/>
</bean>

<!-- 扫描 Mapper 接口 -->
<bean class="com.spring.mybatis.mapper.MapperScannerConfigurer">
    <property name="basePackage" value="com.demo.mybatis"/>
    <property name="sqlSessionFactoryBeanName" value="sqlSessionFactory"/>
</bean>
```

### 2. 注解配置方式

```java
@Configuration
@MapperScan("com.demo.mybatis")
public class MyBatisConfig {
    
    @Bean
    public DataSource dataSource() {
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setUrl("jdbc:mysql://localhost:3306/mybatis");
        dataSource.setUsername("root");
        dataSource.setPassword("123456");
        dataSource.setDriverClassName("com.mysql.jdbc.Driver");
        return dataSource;
    }
    
    @Bean
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(dataSource);
        return factoryBean.getObject();
    }
    
    @Bean
    public SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }
}
```

### 3. 使用 Mapper

```java
@Service
public class UserService {
    
    @Autowired
    private IUserDao userDao;
    
    @Transactional
    public User getUserById(Long id) {
        return userDao.queryUserInfoById(id);
    }
    
    @Transactional
    public void updateUser(User user) {
        userDao.updateUserInfo(user);
    }
}
```

### 4. 继承 SqlSessionDaoSupport

```java
@Repository
public class UserDaoImpl extends SqlSessionDaoSupport implements IUserDao {
    
    @Override
    public User queryUserInfoById(Long id) {
        return getSqlSessionTemplate().selectOne("IUserDao.queryUserInfoById", id);
    }
    
    @Override
    public void updateUserInfo(User user) {
        getSqlSessionTemplate().update("IUserDao.updateUserInfo", user);
    }
}
```

## 事务管理

### 声明式事务

```java
@Service
@Transactional
public class UserService {
    
    @Autowired
    private IUserDao userDao;
    
    public void transferUser(Long fromId, Long toId, BigDecimal amount) {
        // 这些操作会在同一个事务中执行
        userDao.deductBalance(fromId, amount);
        userDao.addBalance(toId, amount);
    }
}
```

### 编程式事务

```java
@Service
public class UserService {
    
    @Autowired
    private TransactionTemplate transactionTemplate;
    
    @Autowired
    private IUserDao userDao;
    
    public void transferUser(Long fromId, Long toId, BigDecimal amount) {
        transactionTemplate.execute(status -> {
            userDao.deductBalance(fromId, amount);
            userDao.addBalance(toId, amount);
            return null;
        });
    }
}
```

## 异常处理

框架会自动将 MyBatis 异常转换为 Spring 的 DataAccessException：

```java
@Service
public class UserService {
    
    @Autowired
    private IUserDao userDao;
    
    public User getUserById(Long id) {
        try {
            return userDao.queryUserInfoById(id);
        } catch (DataAccessException e) {
            // 处理数据访问异常
            logger.error("查询用户失败", e);
            throw new ServiceException("用户查询失败");
        }
    }
}
```

## 配置选项

### MapperScan 注解属性

- `value` / `basePackages`: 要扫描的基础包名
- `sqlSessionFactoryRef`: SqlSessionFactory Bean 名称（默认：sqlSessionFactory）
- `sqlSessionTemplateRef`: SqlSessionTemplate Bean 名称

### MapperScannerConfigurer 属性

- `basePackage`: 要扫描的基础包名
- `sqlSessionFactoryBeanName`: SqlSessionFactory Bean 名称
- `sqlSessionTemplateBeanName`: SqlSessionTemplate Bean 名称

## 最佳实践

1. **使用 @MapperScan 注解**：推荐使用注解方式配置，更加简洁。

2. **合理配置事务**：在 Service 层使用 @Transactional 注解。

3. **异常处理**：统一处理 DataAccessException。

4. **连接池配置**：使用 Druid 等连接池，合理配置连接数。

5. **日志配置**：开启 SQL 日志，便于调试。

## 注意事项

1. 确保 Spring 版本兼容性（推荐 5.3.x）
2. Mapper 接口必须是接口类型
3. 事务传播行为要正确配置
4. 避免在事务方法中捕获异常而不重新抛出

## 示例项目

参考 `src/test/java/com/spring/mybatis/SpringIntegrationTest.java` 中的完整示例。 