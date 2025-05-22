### 返回Insert操作自增索引值
#### 1、需求背景
返回Insert操作自增索引值的需求主要是为了方便用户在插入数据时获取自增主键的值，避免了用户在插入数据后再查询一次的麻烦。注意：当一次数据库操作有2条执行 SQL 语句的时候，重点在于必须在同一个 DB 连接下，否则将会失去事务的特性。因此，在解析完Mapper 配置文件之后，在预处理器中执行 sql 时，需要在同一个 sql 连接下执行。这样就可以保证在同一个事务中执行 sql 语句。

#### 2、方案设计

##### 2.1 整体思路

实现自增主键返回功能需要以下几个关键步骤：

1. 在XML配置中添加对自增主键的支持，允许用户指定需要返回的自增主键
2. 修改Statement的创建方式，使用能够返回自增主键的方式创建PreparedStatement
3. 在执行完插入操作后，获取自增主键并设置到参数对象中
4. 确保整个过程在同一个数据库连接中完成，保证事务的一致性

##### 2.2 具体实现方案

###### 2.2.1 XML配置扩展

在insert标签中添加两个新属性：
- `useGeneratedKeys`: 是否使用自增主键（布尔值）
- `keyProperty`: 指定自增主键对应的参数对象属性名

示例：
```xml
<insert id="insertUser" parameterType="com.demo.mybatis.User" useGeneratedKeys="true" keyProperty="id">
    INSERT INTO user (name, age) VALUES (#{name}, #{age})
</insert>
```

###### 2.2.2 MappedStatement扩展

在MappedStatement类中添加对应的属性：
- `useGeneratedKeys`: 是否使用自增主键
- `keyProperty`: 自增主键对应的属性名

###### 2.2.3 Statement创建修改

修改PreparedStatementHandler的instantiateStatement方法，当useGeneratedKeys为true时，使用Connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)创建PreparedStatement。

###### 2.2.4 自增主键处理

在执行完插入操作后，通过Statement.getGeneratedKeys()获取自增主键，并通过反射或MetaObject设置到参数对象中。

###### 2.2.5 事务一致性保证

确保整个过程在同一个数据库连接中完成，这一点通过现有的事务管理机制已经能够保证。

##### 2.3 核心类修改

需要修改的核心类包括：
1. `MappedStatement`: 添加自增主键相关属性
2. `XMLStatementBuilder`: 解析XML中的自增主键配置
3. `PreparedStatementHandler`: 修改创建Statement的方式并处理自增主键
4. `KeyGenerator`: 新增类，用于处理自增主键

##### 2.4 详细设计

###### 2.4.1 MappedStatement类修改

```java
public class MappedStatement {
    // 现有属性
    private Configuration configuration;
    private String id;
    private SqlCommandType sqlCommandType;
    private SqlSource sqlSource;
    Class<?> resultType;
    private LanguageDriver lang;
    private String resultMap;

    // 新增属性
    private boolean useGeneratedKeys;
    private String keyProperty;

    // 构造函数等保持不变

    public static class Builder {
        private MappedStatement mappedStatement = new MappedStatement();

        // 现有构造函数保持不变

        // 新增方法
        public Builder useGeneratedKeys(boolean useGeneratedKeys) {
            mappedStatement.useGeneratedKeys = useGeneratedKeys;
            return this;
        }

        public Builder keyProperty(String keyProperty) {
            mappedStatement.keyProperty = keyProperty;
            return this;
        }

        // build方法保持不变
    }

    // 新增getter方法
    public boolean isUseGeneratedKeys() {
        return useGeneratedKeys;
    }

    public String getKeyProperty() {
        return keyProperty;
    }

    // 其他getter方法保持不变
}
```

###### 2.4.2 XMLStatementBuilder类修改

```java
public class XMLStatementBuilder {
    // 现有属性和构造函数保持不变

    public void parseStatementNode() {
        // 现有代码保持不变

        // 解析useGeneratedKeys和keyProperty属性
        String nodeName = element.getName();
        if ("insert".equals(nodeName)) {
            String useGeneratedKeys = element.attributeValue("useGeneratedKeys");
            String keyProperty = element.attributeValue("keyProperty");

            if (useGeneratedKeys != null) {
                statementBuilder.useGeneratedKeys(Boolean.parseBoolean(useGeneratedKeys));
            }

            if (keyProperty != null) {
                statementBuilder.keyProperty(keyProperty);
            }
        }

        // 其余代码保持不变
        MappedStatement mappedStatement = statementBuilder.build();
        configuration.addMappedStatement(mappedStatement);
    }
}
```

###### 2.4.3 KeyGenerator接口及实现

```java
// 新增接口
public interface KeyGenerator {
    void processGeneratedKeys(Statement statement, Object parameter, MappedStatement mappedStatement);
}

// 默认实现
public class Jdbc3KeyGenerator implements KeyGenerator {
    @Override
    public void processGeneratedKeys(Statement statement, Object parameter, MappedStatement mappedStatement) {
        if (parameter != null && mappedStatement.isUseGeneratedKeys()) {
            try {
                ResultSet rs = statement.getGeneratedKeys();
                if (rs.next()) {
                    String keyProperty = mappedStatement.getKeyProperty();
                    if (keyProperty != null && !keyProperty.isEmpty()) {
                        MetaObject metaParam = mappedStatement.getConfiguration().newMetaObject(parameter);
                        if (metaParam.hasSetter(keyProperty)) {
                            Class<?> keyType = metaParam.getSetterType(keyProperty);
                            // 根据属性类型获取对应的值
                            Object value = getValueByType(rs, keyType);
                            metaParam.setValue(keyProperty, value);
                        }
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException("Error getting generated keys.", e);
            }
        }
    }

    private Object getValueByType(ResultSet rs, Class<?> type) throws SQLException {
        if (type == Integer.class || type == int.class) {
            return rs.getInt(1);
        } else if (type == Long.class || type == long.class) {
            return rs.getLong(1);
        } else if (type == String.class) {
            return rs.getString(1);
        } else {
            return rs.getObject(1);
        }
    }
}
```

###### 2.4.4 PreparedStatementHandler类修改

```java
public class PreparedStatementHandler extends BaseStatementHandler {
    // 现有属性和构造函数保持不变

    @Override
    protected Statement instantiateStatement(Connection connection) throws SQLException {
        String sql = boundSql.getSql();

        // 检查是否需要返回自增主键
        if (mappedStatement.isUseGeneratedKeys() && mappedStatement.getSqlCommandType() == SqlCommandType.INSERT) {
            return connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        } else {
            return connection.prepareStatement(sql);
        }
    }

    @Override
    public int update(Statement statement) throws SQLException {
        PreparedStatement ps = (PreparedStatement) statement;
        ps.execute();
        int rows = ps.getUpdateCount();

        // 处理自增主键
        if (mappedStatement.isUseGeneratedKeys() && mappedStatement.getSqlCommandType() == SqlCommandType.INSERT) {
            KeyGenerator keyGenerator = new Jdbc3KeyGenerator();
            keyGenerator.processGeneratedKeys(ps, parameterObject, mappedStatement);
        }

        return rows;
    }

    // 其他方法保持不变
}
```

##### 2.5 使用示例

###### 2.5.1 XML配置示例

```xml
<mapper namespace="com.demo.mybatis.UserMapper">
    <insert id="insertUser" parameterType="com.demo.mybatis.User" useGeneratedKeys="true" keyProperty="id">
        INSERT INTO user (name, age) VALUES (#{name}, #{age})
    </insert>
</mapper>
```

###### 2.5.2 Java代码示例

```java
public interface UserMapper {
    int insertUser(User user);
}

public class User {
    private Long id;
    private String name;
    private Integer age;

    // Getter和Setter方法
}

// 使用示例
SqlSession sqlSession = sqlSessionFactory.openSession();
try {
    UserMapper userMapper = sqlSession.getMapper(UserMapper.class);
    User user = new User();
    user.setName("张三");
    user.setAge(25);

    // 执行插入操作
    userMapper.insertUser(user);

    // 此时user对象的id属性已经被自动设置为数据库生成的自增主键值
    System.out.println("插入用户的ID: " + user.getId());

    sqlSession.commit();
} finally {
    sqlSession.close();
}
```

##### 2.6 实现步骤

1. 添加KeyGenerator接口和Jdbc3KeyGenerator实现类
2. 修改MappedStatement类，添加useGeneratedKeys和keyProperty属性及相关方法
3. 修改XMLStatementBuilder类，解析XML中的useGeneratedKeys和keyProperty属性
4. 修改PreparedStatementHandler类，支持创建能返回自增主键的PreparedStatement，并在执行后处理自增主键

##### 2.7 注意事项

1. 自增主键功能仅适用于支持自增主键的数据库（如MySQL、SQL Server等）
2. 需要确保XML配置中的keyProperty属性与Java对象中的属性名一致
3. 对于批量插入操作，可能需要特殊处理，本实现主要针对单条插入
4. 确保在同一个事务中执行，以保证数据一致性

#### 3、总结

本方案通过扩展small-mybatis框架，实现了在执行INSERT操作后自动获取并设置自增主键值的功能。主要通过以下几个方面实现：

1. **配置扩展**：在XML配置中添加useGeneratedKeys和keyProperty属性，使用户可以灵活配置是否需要返回自增主键以及自增主键对应的属性名。

2. **核心类扩展**：修改MappedStatement、XMLStatementBuilder和PreparedStatementHandler等核心类，添加对自增主键的支持。

3. **新增KeyGenerator**：引入KeyGenerator接口及其实现类Jdbc3KeyGenerator，专门处理自增主键的获取和设置。

4. **事务一致性**：利用现有的事务管理机制，确保在同一个数据库连接中执行操作，保证事务的一致性。

该实现遵循了MyBatis的设计理念和架构，通过扩展而非修改的方式添加新功能，保持了框架的稳定性和兼容性。同时，该功能的实现也为用户提供了更便捷的数据操作方式，避免了手动查询自增主键的麻烦。
