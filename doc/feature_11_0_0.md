### 通过注解配置执行 sql 语句

#### 1. 需求背景
目前 small-mybatis 只支持 XML 配置的方式来执行 sql 语句，使用起来不够灵活。为了让用户可以通过注解的方式来执行 sql 语句，需要对框架进行改造。扩展框架的功能，支持通过注解的方式来执行 sql 语句。这样用户就可以在 Mapper 接口中直接使用注解来定义 sql 语句，而不需要再编写 XML 文件。

#### 2. 方案设计

##### 2.1 SQL 注解
我们创建了四个用于 SQL 语句的注解：
- `@Select`：用于 SELECT 语句
- `@Insert`：用于 INSERT 语句
- `@Update`：用于 UPDATE 语句
- `@Delete`：用于 DELETE 语句

每个注解都接受一个 SQL 字符串作为其值。

##### 2.2 参数注解
我们创建了一个 `@Param` 注解，用于指定方法参数的名称：

```java
@Param("id") Integer id
```

##### 2.3 注解处理
我们实现了一个 `MapperAnnotationBuilder` 类，它扫描 Mapper 接口中的注解，并为每个带注解的方法创建 `MappedStatement` 对象。

##### 2.4 语言驱动
我们创建了一个 `AnnotationLanguageDriver`，用于从注解值创建 `SqlSource` 对象。

##### 2.5 与现有代码集成
我们修改了以下组件以支持注解：
- `MapperRegistry`：现在在添加 Mapper 时扫描注解
- `MapperMethod`：现在在创建 SQL 命令时检查注解
- `LanguageDriver`：添加了一个新方法，用于从字符串创建 SqlSource

#### 3. 架构设计决策

##### 3.1 关注点分离
- `MapperAnnotationBuilder`：负责解析注解
- `AnnotationLanguageDriver`：负责从注解值创建 SQL 源

##### 3.2 兼容性
- 实现保持与现有基于 XML 的 SQL 执行的兼容性
- 基于 XML 和基于注解的 Mapper 可以在同一应用程序中使用

##### 3.3 可扩展性
- 设计允许未来的扩展，例如用于结果映射的其他注解

#### 4. 使用示例

##### 4.1 基本 CRUD 操作

```java
public interface UserMapper {
    @Select("SELECT * FROM user WHERE id = #{id}")
    User getUserById(@Param("id") Integer id);

    @Insert("INSERT INTO user(name, age) VALUES(#{name}, #{age})")
    int insertUser(User user);

    @Update("UPDATE user SET name = #{name}, age = #{age} WHERE id = #{id}")
    int updateUser(User user);

    @Delete("DELETE FROM user WHERE id = #{id}")
    int deleteUser(@Param("id") Integer id);
}
```

##### 4.2 多参数

```java
public interface UserMapper {
    @Select("SELECT * FROM user WHERE name = #{name} AND age > #{minAge}")
    List<User> getUsersByNameAndAge(@Param("name") String name, @Param("minAge") Integer minAge);
}
```

#### 5. 配置

不需要额外的配置。当您注册 Mapper 接口时，注解支持会自动启用：

```java
SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
sqlSessionFactory.getConfiguration().addMapper(UserMapper.class);
```

或者当您注册一个包时：

```java
sqlSessionFactory.getConfiguration().addMappers("com.example.mappers");
```
