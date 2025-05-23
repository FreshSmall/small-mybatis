### 二级缓存

#### 1.需求背景

在feature_16_0_0.md中我们完成了 Mybatis 框架中关于一级缓存的功能实现，它对数据的缓存操作主要作用于一次 Session 会话的生命周期内，从查询开始保存数据，到执行有可能变更数据库的操作为止清空一级缓存数据。

那么关于缓存的实现，如果我们希望于当会话结束后，再发起的会话还是相同的查询操作，最好也是可以把数据从缓存中获取出来。这个时候该如何实现呢？其实这部分内容就是 Mybatis 框架中的二级缓存，以一个 Mapper 为生命周期，在这个 Mapper 内的同一个操作，无论发起几次会话都可以使用缓存来处理数据。

之所以这个操作称之为二级缓存，是因为它在一级缓存会话层上，添加的额外缓存操作，当会话发生 close、commit 操作时则把数据刷新到二级缓存中进行保存，直到执行器发生 update 操作时清空缓存。

#### 2.需求分析

基于现有的一级缓存实现，二级缓存需要满足以下核心需求：

1. **生命周期管理**：二级缓存以 Mapper 为单位，生命周期跨越多个 Session 会话
2. **缓存层级**：在一级缓存基础上增加二级缓存，形成两级缓存体系
3. **数据同步机制**：
   - Session 的 commit/close 操作时，将一级缓存数据同步到二级缓存
   - 查询时先检查一级缓存，再检查二级缓存
4. **缓存失效策略**：
   - 执行 update/insert/delete 操作时清空对应 Mapper 的二级缓存
   - 保证数据一致性
5. **配置控制**：
   - 支持在 Mapper 级别启用/禁用二级缓存
   - 提供缓存配置选项

#### 3.方案设计

##### 3.1 总体架构设计

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   SqlSession    │───▶│   一级缓存       │───▶│   二级缓存       │
│   (会话级别)     │    │  (Session级)    │    │  (Mapper级)     │
└─────────────────┘    └─────────────────┘    └─────────────────┘
        │                       │                       │
        │                       │                       │
        ▼                       ▼                       ▼
  Session生命周期           BaseExecutor              CacheManager
   - commit()              - localCache              - mapperCaches
   - close()               - query()                 - 跨Session共享
   - rollback()            - update()                - 失效策略
```

##### 3.2 核心组件设计

**1. CacheManager（缓存管理器）**
- 负责管理所有 Mapper 的二级缓存实例
- 提供缓存的创建、获取、清理功能
- 维护 Mapper ID 到 Cache 实例的映射关系

**2. CachingExecutor（缓存执行器装饰器）**
- 装饰现有的 Executor，增加二级缓存功能
- 实现缓存查询逻辑：一级缓存 → 二级缓存 → 数据库
- 处理缓存的写入和失效逻辑

**3. TransactionalCache（事务缓存包装器）**
- 包装二级缓存，提供事务性缓存操作
- 支持延迟写入（commit 时才真正写入二级缓存）
- 提供回滚机制

##### 3.3 详细设计

**3.3.1 缓存管理器设计**

```java
public class CacheManager {
    // Mapper ID -> Cache 实例的映射
    private final Map<String, Cache> mapperCaches;
    
    // 获取或创建 Mapper 对应的二级缓存
    public Cache getOrCreateCache(String mapperId);
    
    // 清空指定 Mapper 的二级缓存
    public void clearCache(String mapperId);
    
    // 清空所有二级缓存
    public void clearAllCache();
}
```

**3.3.2 缓存执行器设计**

```java
public class CachingExecutor implements Executor {
    private final Executor delegate;
    private final CacheManager cacheManager;
    private final Map<String, TransactionalCache> transactionalCaches;
    
    // 查询逻辑：一级缓存 → 二级缓存 → 数据库
    public <E> List<E> query(MappedStatement ms, Object parameter, 
                           ResultHandler resultHandler, BoundSql boundSql);
    
    // 更新操作：清空相关二级缓存
    public int update(MappedStatement ms, Object parameter);
    
    // 提交操作：将一级缓存数据刷新到二级缓存
    public void commit(boolean required);
    
    // 关闭操作：处理缓存的最终同步
    public void close(boolean forceRollback);
}
```

**3.3.3 事务缓存包装器设计**

```java
public class TransactionalCache {
    private final Cache delegate;
    private final Map<Object, Object> entriesToAddOnCommit;
    private final Set<Object> entriesMissedInCache;
    private boolean clearOnCommit;
    
    // 暂存待提交的缓存项
    public void putObject(Object key, Object value);
    
    // 从缓存获取数据
    public Object getObject(Object key);
    
    // 提交时将暂存数据写入真实缓存
    public void commit();
    
    // 回滚时清空暂存数据
    public void rollback();
}
```

##### 3.4 实现流程

**3.4.1 查询流程**
1. 检查一级缓存（Session 级别）
2. 如果一级缓存未命中，检查二级缓存（Mapper 级别）
3. 如果二级缓存命中，将数据放入一级缓存并返回
4. 如果二级缓存未命中，执行数据库查询
5. 将查询结果放入一级缓存

**3.4.2 提交流程**
1. Session commit() 触发
2. 将一级缓存中的数据通过 TransactionalCache 暂存
3. TransactionalCache commit() 将暂存数据写入二级缓存
4. 清空一级缓存

**3.4.3 更新流程**
1. 执行 update/insert/delete 操作
2. 清空对应 Mapper 的二级缓存
3. 清空一级缓存
4. 执行实际的数据库更新操作

##### 3.5 配置扩展

**3.5.1 MappedStatement 扩展**
```java
public class MappedStatement {
    // 新增：是否启用二级缓存
    private boolean cacheEnabled = false;
    
    // 新增：缓存配置
    private Cache cache;
}
```

**3.5.2 Configuration 扩展**
```java
public class Configuration {
    // 新增：缓存管理器
    protected CacheManager cacheManager = new CacheManager();
    
    // 新增：全局二级缓存开关
    protected boolean cacheEnabled = true;
}
```

##### 3.6 集成点

1. **SqlSessionFactory**：创建 SqlSession 时使用 CachingExecutor 装饰原有 Executor
2. **MappedStatement.Builder**：支持配置二级缓存相关属性
3. **Configuration**：添加缓存管理器和全局配置
4. **XMLMapperBuilder**：解析 Mapper XML 中的缓存配置

##### 3.7 扩展性考虑

1. **缓存实现可插拔**：支持不同的 Cache 实现（如 Redis、Ehcache 等）
2. **缓存策略可配置**：支持 LRU、FIFO 等不同的淘汰策略
3. **监控和统计**：提供缓存命中率、大小等统计信息
4. **序列化支持**：为分布式缓存提供对象序列化机制

##### 3.8 注意事项

1. **线程安全**：确保二级缓存在多线程环境下的安全性
2. **内存管理**：避免缓存导致的内存泄漏问题
3. **数据一致性**：确保缓存与数据库数据的一致性
4. **性能影响**：合理控制缓存的大小和策略，避免性能下降
