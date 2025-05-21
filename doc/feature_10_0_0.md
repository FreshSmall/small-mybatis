### 完善 ORM 框架，增删改操作
#### 1. 需求背景
目前 small-mybatis 基本实现了一个框架结构，能够完成基本的查询操作，以及处理参数和返回结果。但是目前这个框架只有一个 select 操作，还没有实现增、删、改操作。为了完善这个框架，需要实现 insert、update 和 delete 以及 select 返回的集合类型数据。

#### 2. 方案设计
 新增的方法和select 一样，也是在 SqlSession 需要定义新的接口，通知让这些接口被映射器类方法 MapperMethod 进行调用处理。在 XMLMapperBuilder 新增解析 insert/update/delete的解析，后续 DefaultSqlSession 中新增的执行 SQL 方法 insert/update/delete 就可以通过 Configuration 配置项拿到对应的映射器语句，并执行后续的处理流程。

值得注意的是，除了我们已经开发完的 DefaultSqlSession#select 方法，其他定义的 insert、delete、update，都是调用内部的 update 方法，这也是 Mybatis ORM 框架对此类语句处理的一个包装。因为除了 select 方法，insert、delete、update，都是共性处理逻辑，所以可以被包装成一个逻辑来处理。
