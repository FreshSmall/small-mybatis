### 解析和使用 ResultMap 映射参数配置

#### 1. 需求背景
在数据库中，表的字段和 Java 对象的属性名称不一定一致，因此需要一种机制来将数据库字段映射到 Java 对象的属性。MyBatis 提供了 ResultMap 来实现这种映射关系。注意：small-mybatis 中也可以使用例如 employee_name as employeeName 的方式进行处理，但在整个编程中并不是太优雅，因为所有的查询都要做 as 映射，那么使用一个统一的字段映射更加合理。

#### 2. 方案设计

##### 2.1 整体架构

为了实现 ResultMap 映射参数配置功能，我们需要扩展 small-mybatis 框架，添加以下核心组件：

1. **ResultMap**：用于定义数据库列名与 Java 对象属性之间的映射关系
2. **ResultMapping**：表示单个列与属性之间的映射关系
3. **ResultMapResolver**：负责解析 ResultMap 配置并创建 ResultMap 对象
4. **XMLMapperBuilder 扩展**：增加对 `<resultMap>` 元素的解析支持
5. **DefaultResultSetHandler 扩展**：增强结果集处理逻辑，支持使用 ResultMap 进行映射

整体架构如下图所示：

```
                  ┌─────────────────┐
                  │  XMLMapperBuilder│
                  └────────┬────────┘
                           │ 解析
                           ▼
┌─────────────┐    ┌─────────────────┐    ┌─────────────────┐
│ResultMapping│◄───│    ResultMap    │◄───│ResultMapResolver│
└─────────────┘    └────────┬────────┘    └─────────────────┘
                           │ 使用
                           ▼
                  ┌─────────────────┐
                  │DefaultResultSet-│
                  │    Handler      │
                  └─────────────────┘
```

##### 2.2 核心类设计

###### 2.2.1 ResultMap 类

ResultMap 类用于存储结果映射的配置信息，包括 id、type 以及列与属性的映射关系。

```java
public class ResultMap {
    private String id;
    private Class<?> type;
    private List<ResultMapping> resultMappings;
    private Set<String> mappedColumns;

    // 构造函数、Getter 方法等

    public static class Builder {
        private ResultMap resultMap = new ResultMap();

        public Builder(String id, Class<?> type, List<ResultMapping> resultMappings) {
            resultMap.id = id;
            resultMap.type = type;
            resultMap.resultMappings = resultMappings;
            resultMap.mappedColumns = new HashSet<>();
            for (ResultMapping resultMapping : resultMappings) {
                resultMap.mappedColumns.add(resultMapping.getColumn().toUpperCase(Locale.ENGLISH));
            }
        }

        public ResultMap build() {
            return resultMap;
        }
    }
}
```

###### 2.2.2 ResultMapping 类

ResultMapping 类表示单个列与属性之间的映射关系。

```java
public class ResultMapping {
    private String property;
    private String column;
    private Class<?> javaType;
    private JdbcType jdbcType;

    // 构造函数、Getter 方法等

    public static class Builder {
        private ResultMapping resultMapping = new ResultMapping();

        public Builder(String property, String column) {
            resultMapping.property = property;
            resultMapping.column = column;
        }

        public Builder javaType(Class<?> javaType) {
            resultMapping.javaType = javaType;
            return this;
        }

        public Builder jdbcType(JdbcType jdbcType) {
            resultMapping.jdbcType = jdbcType;
            return this;
        }

        public ResultMapping build() {
            return resultMapping;
        }
    }
}
```

###### 2.2.3 MappedStatement 类扩展

扩展 MappedStatement 类，添加 resultMap 属性。

```java
public class MappedStatement {
    // 现有属性
    private Configuration configuration;
    private String id;
    private SqlCommandType sqlCommandType;
    private SqlSource sqlSource;
    Class<?> resultType;
    private LanguageDriver lang;

    // 新增属性
    private String resultMap;

    // 构造函数、Getter 方法等

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

        public MappedStatement build() {
            assert mappedStatement.configuration != null;
            assert mappedStatement.id != null;
            return mappedStatement;
        }
    }
}
```

##### 2.3 XML 解析实现

###### 2.3.1 XMLMapperBuilder 扩展

扩展 XMLMapperBuilder 类，添加对 `<resultMap>` 元素的解析支持。

```java
public class XMLMapperBuilder extends BaseBuilder {
    // 现有代码...

    private void configurationElement(Element element) {
        // 1.配置namespace
        currentNamespace = element.attributeValue("namespace");
        if (currentNamespace.equals("")) {
            throw new RuntimeException("Mapper's namespace cannot be empty");
        }

        // 2.配置resultMap
        buildResultMapElements(element.elements("resultMap"));

        // 3.配置select|insert|update|delete
        buildStatementFromContext(element.elements("select"));
        buildStatementFromContext(element.elements("insert"));
        buildStatementFromContext(element.elements("update"));
        buildStatementFromContext(element.elements("delete"));
    }

    private void buildResultMapElements(List<Element> list) {
        for (Element element : list) {
            try {
                resultMapElement(element);
            } catch (Exception e) {
                throw new RuntimeException("Error parsing resultMap element: " + e, e);
            }
        }
    }

    private void resultMapElement(Element resultMapNode) throws Exception {
        String id = resultMapNode.attributeValue("id");
        String type = resultMapNode.attributeValue("type");
        Class<?> typeClass = resolveClass(type);

        List<ResultMapping> resultMappings = new ArrayList<>();

        // 解析 result 元素
        List<Element> resultChildren = resultMapNode.elements("result");
        for (Element resultChild : resultChildren) {
            ResultMapping resultMapping = buildResultMapping(resultChild, typeClass);
            resultMappings.add(resultMapping);
        }

        // 创建 ResultMap
        String resultMapId = currentNamespace + "." + id;
        ResultMap resultMap = new ResultMap.Builder(resultMapId, typeClass, resultMappings).build();
        configuration.addResultMap(resultMap);
    }

    private ResultMapping buildResultMapping(Element resultChild, Class<?> typeClass) {
        String property = resultChild.attributeValue("property");
        String column = resultChild.attributeValue("column");
        String javaType = resultChild.attributeValue("javaType");
        String jdbcType = resultChild.attributeValue("jdbcType");

        Class<?> javaTypeClass = resolveClass(javaType);
        JdbcType jdbcTypeEnum = resolveJdbcType(jdbcType);

        ResultMapping.Builder builder = new ResultMapping.Builder(property, column);
        if (javaTypeClass != null) {
            builder.javaType(javaTypeClass);
        }
        if (jdbcTypeEnum != null) {
            builder.jdbcType(jdbcTypeEnum);
        }

        return builder.build();
    }
}
```

###### 2.3.2 XMLStatementBuilder 扩展

扩展 XMLStatementBuilder 类，添加对 resultMap 属性的解析支持。

```java
public class XMLStatementBuilder extends BaseBuilder {
    // 现有代码...

    public void parseStatementNode() {
        String id = element.attributeValue("id");
        // 参数类型
        String parameterType = element.attributeValue("parameterType");
        Class<?> parameterTypeClass = resolveAlias(parameterType);
        // 结果类型
        String resultType = element.attributeValue("resultType");
        Class<?> resultTypeClass = resolveAlias(resultType);
        // 结果映射
        String resultMap = element.attributeValue("resultMap");

        // 获取命令类型(select|insert|update|delete)
        String nodeName = element.getName();
        SqlCommandType sqlCommandType = SqlCommandType.valueOf(nodeName.toUpperCase(Locale.ENGLISH));

        // 获取默认语言驱动器
        Class<?> langClass = configuration.getLanguageRegistry().getDefaultDriverClass();
        LanguageDriver langDriver = configuration.getLanguageRegistry().getDriver(langClass);

        SqlSource sqlSource = langDriver.createSqlSource(configuration, element, parameterTypeClass);

        MappedStatement.Builder statementBuilder = new MappedStatement.Builder(configuration, currentNamespace + "." + id, sqlCommandType, sqlSource, resultTypeClass);

        // 设置 resultMap
        if (resultMap != null) {
            statementBuilder.resultMap(resultMap);
        }

        MappedStatement mappedStatement = statementBuilder.build();
        // 添加解析 SQL
        configuration.addMappedStatement(mappedStatement);
    }
}
```

##### 2.4 结果集处理实现

###### 2.4.1 Configuration 类扩展

扩展 Configuration 类，添加 resultMaps 属性和相关方法。

```java
public class Configuration {
    // 现有属性...

    // 新增属性
    protected final Map<String, ResultMap> resultMaps = new HashMap<>();

    // 现有方法...

    // 新增方法
    public void addResultMap(ResultMap resultMap) {
        resultMaps.put(resultMap.getId(), resultMap);
    }

    public ResultMap getResultMap(String id) {
        return resultMaps.get(id);
    }

    public boolean hasResultMap(String id) {
        return resultMaps.containsKey(id);
    }
}
```

###### 2.4.2 DefaultResultSetHandler 扩展

扩展 DefaultResultSetHandler 类，增强结果集处理逻辑，支持使用 ResultMap 进行映射。

```java
public class DefaultResultSetHandler implements ResultSetHandler {
    // 现有代码...

    @Override
    public <E> List<E> handleResultSets(Statement stmt) throws SQLException {
        // 获取结果集
        ResultSet rs = stmt.getResultSet();
        if (rs == null) {
            return new ArrayList<>();
        }

        // 包装结果集
        ResultSetWrapper rsw = new ResultSetWrapper(rs, configuration);

        // 获取返回类型
        Class<?> resultType = mappedStatement.getResultType();

        // 检查是否有 ResultMap
        String resultMapId = mappedStatement.getResultMap();
        ResultMap resultMap = null;
        if (resultMapId != null) {
            resultMap = configuration.getResultMap(resultMapId);
        }

        // 检查返回类型是否为集合类型
        if (resultType == List.class || resultType == java.util.Collection.class) {
            // 如果是集合类型，则使用 Object.class 作为元素类型
            resultType = Object.class;
        }

        // 处理结果集
        return handleResultSet(rsw, resultType, resultMap);
    }

    private <E> List<E> handleResultSet(ResultSetWrapper rsw, Class<?> resultType, ResultMap resultMap) throws SQLException {
        List<E> resultList = new ArrayList<>();
        ResultSet rs = rsw.getResultSet();

        // 处理基本类型
        if (isPrimitiveOrWrapper(resultType)) {
            return handlePrimitiveTypeResult(rsw, resultType);
        }

        // 处理对象类型
        while (rs.next()) {
            @SuppressWarnings("unchecked")
            E rowObject = (E) handleRowValues(rsw, resultType, resultMap);
            resultList.add(rowObject);
        }

        return resultList;
    }

    private <T> T handleRowValues(ResultSetWrapper rsw, Class<T> resultType, ResultMap resultMap) throws SQLException {
        try {
            // 创建结果对象实例
            T resultObject = resultType.getDeclaredConstructor().newInstance();

            // 创建元对象，用于设置属性值
            MetaObject metaObject = configuration.newMetaObject(resultObject);

            if (resultMap != null) {
                // 使用 ResultMap 进行映射
                applyResultMap(rsw, resultMap, metaObject);
            } else {
                // 使用列名直接映射
                applyColumnNames(rsw, metaObject);
            }

            return resultObject;
        } catch (Exception e) {
            throw new RuntimeException("Error creating result object: " + e.getMessage(), e);
        }
    }

    private void applyResultMap(ResultSetWrapper rsw, ResultMap resultMap, MetaObject metaObject) throws SQLException {
        for (ResultMapping resultMapping : resultMap.getResultMappings()) {
            String column = resultMapping.getColumn();
            String property = resultMapping.getProperty();

            if (rsw.getColumnNames().contains(column.toUpperCase(Locale.ENGLISH))) {
                // 检查属性是否存在
                if (metaObject.hasSetter(property)) {
                    Class<?> propertyType = metaObject.getSetterType(property);

                    // 获取对应类型的TypeHandler
                    TypeHandler<?> typeHandler = rsw.getTypeHandler(propertyType, column);

                    // 使用TypeHandler获取值并设置到对象中
                    Object value = typeHandler.getResult(rsw.getResultSet(), column);
                    if (value != null || !propertyType.isPrimitive()) {
                        metaObject.setValue(property, value);
                    }
                }
            }
        }
    }

    private void applyColumnNames(ResultSetWrapper rsw, MetaObject metaObject) throws SQLException {
        // 获取结果集的列名
        List<String> columnNames = rsw.getColumnNames();

        // 遍历所有列，设置对应的属性值
        for (String columnName : columnNames) {
            // 尝试找到对应的属性名
            String propertyName = columnName;

            // 检查属性是否存在
            if (metaObject.hasSetter(propertyName)) {
                Class<?> propertyType = metaObject.getSetterType(propertyName);

                // 获取对应类型的TypeHandler
                TypeHandler<?> typeHandler = rsw.getTypeHandler(propertyType, columnName);

                // 使用TypeHandler获取值并设置到对象中
                Object value = typeHandler.getResult(rsw.getResultSet(), columnName);
                if (value != null || !propertyType.isPrimitive()) {
                    metaObject.setValue(propertyName, value);
                }
            }
        }
    }
}
```

##### 2.5 使用示例

###### 2.5.1 XML 配置示例

```xml
<mapper namespace="com.demo.mybatis.UserMapper">
    <resultMap id="userResultMap" type="com.demo.mybatis.User">
        <result property="id" column="id"/>
        <result property="name" column="user_name"/>
        <result property="age" column="user_age"/>
    </resultMap>

    <select id="selectUser" resultMap="userResultMap">
        SELECT id, user_name, user_age FROM user WHERE id = #{id}
    </select>
</mapper>
```

###### 2.5.2 Java 代码示例

```java
public interface UserMapper {
    User selectUser(Long id);
}

public class User {
    private Long id;
    private String name;
    private Integer age;

    // Getter 和 Setter 方法
}

// 使用示例
SqlSession sqlSession = sqlSessionFactory.openSession();
UserMapper userMapper = sqlSession.getMapper(UserMapper.class);
User user = userMapper.selectUser(1L);
```

#### 3. 实现步骤

1. 创建 ResultMap 和 ResultMapping 类
2. 扩展 MappedStatement 类，添加 resultMap 属性
3. 扩展 Configuration 类，添加 resultMaps 属性和相关方法
4. 扩展 XMLMapperBuilder 类，添加对 `<resultMap>` 元素的解析支持
5. 扩展 XMLStatementBuilder 类，添加对 resultMap 属性的解析支持
6. 扩展 DefaultResultSetHandler 类，增强结果集处理逻辑，支持使用 ResultMap 进行映射
7. 编写测试用例，验证功能是否正常

#### 4. 注意事项

1. ResultMap 的 id 需要加上命名空间前缀，以确保唯一性
2. 在解析 ResultMap 时，需要考虑继承关系（extends 属性）
3. 在处理结果集时，需要优先使用 ResultMap 进行映射，如果没有 ResultMap，则使用列名直接映射
4. 需要考虑大小写敏感性，数据库列名通常不区分大小写，而 Java 属性名区分大小写
5. 需要考虑嵌套映射（association、collection）的支持，但这可以作为后续扩展功能
