### 解析含标签的动态SQL语句
#### 1、需求背景
我们在 Mapper XML 中配置的 SQL 一直都是静态 SQL 语句，也就是说测试的是一条完整的 SQL 语句，例如：select * from table where id = ? 那么其实在实际的使用场景中，我们往往会需要根据入参对象中的字段是否有值，判断后才被设置到 SQL 语句上。例如：
```xml
<select id="selectUser" parameterType="com.demo.mybatis.User">
    SELECT * FROM user
    <if test="name != null">
        WHERE name = #{name}
    </if>
    <if test="age != null">
        AND age = #{age}
    </if>
</select>
```
在上面的 SQL 语句中，我们使用了 `<if>` 标签来判断入参对象中的 `name` 和 `age` 字段是否有值，如果有值就将其添加到 SQL 语句中。这样就可以实现动态 SQL 的效果。

#### 2、方案设计

##### 2.1 整体设计思路

动态 SQL 的核心是在 SQL 语句中嵌入条件判断标签（如 `<if>`），根据参数对象的属性值动态生成最终的 SQL 语句。实现这一功能需要：

1. **解析 XML 中的标签**：识别并解析 SQL 语句中的动态标签
2. **条件表达式求值**：评估标签中的条件表达式是否为真
3. **动态 SQL 组装**：根据条件评估结果，动态组装最终的 SQL 语句

我们将基于现有的 `SqlNode` 体系进行扩展，为每种动态标签创建对应的 `SqlNode` 实现类。

##### 2.2 核心类设计

###### 2.2.1 SqlNode 体系扩展

现有的 `SqlNode` 接口定义了 SQL 节点的基本行为，我们将扩展这个体系来支持动态 SQL：

```
SqlNode (接口)
├── StaticTextSqlNode (静态文本节点)
├── MixedSqlNode (混合节点，包含多个子节点)
├── TextSqlNode (可能包含 ${} 变量的文本节点) [新增]
├── IfSqlNode (条件判断节点) [新增]
└── TrimSqlNode (用于处理前缀/后缀的节点) [可选扩展]
```

###### 2.2.2 表达式求值器

为了评估 `<if test="...">` 中的条件表达式，我们需要一个表达式求值器：

```java
public class ExpressionEvaluator {
    // 评估表达式是否为真
    public boolean evaluateBoolean(String expression, Object parameterObject);

    // 计算表达式的值
    public Object evaluateObject(String expression, Object parameterObject);
}
```

我们将使用 OGNL (Object Graph Navigation Language) 作为表达式语言，它能够访问对象的属性和方法。

###### 2.2.3 动态 SQL 源

为了支持动态 SQL，我们需要扩展 `SqlSource` 体系：

```
SqlSource (接口)
├── StaticSqlSource (静态 SQL 源)
├── RawSqlSource (原始 SQL 源)
└── DynamicSqlSource (动态 SQL 源) [新增]
```

`DynamicSqlSource` 将在运行时根据参数对象动态生成 SQL 语句。

##### 2.3 XML 解析流程

我们需要修改 `XMLScriptBuilder` 类的 `parseDynamicTags` 方法，使其能够识别和处理 XML 中的动态标签：

1. 解析元素的文本内容为 `TextSqlNode` 或 `StaticTextSqlNode`
2. 解析子元素（如 `<if>`）为对应的 `SqlNode` 实现
3. 将所有节点组合为 `MixedSqlNode`

##### 2.4 动态 SQL 执行流程

1. `XMLLanguageDriver` 创建 `XMLScriptBuilder` 解析 XML 元素
2. `XMLScriptBuilder` 解析出 SQL 节点树，并创建 `DynamicSqlSource`
3. 执行 SQL 时，`DynamicSqlSource` 根据参数对象创建 `DynamicContext`
4. SQL 节点树应用到 `DynamicContext`，生成最终的 SQL 语句
5. 生成的 SQL 语句被解析为 `BoundSql`，并执行

##### 2.5 反射机制在动态 SQL 中的应用

动态 SQL 的核心是通过反射机制访问参数对象的属性。在 small-mybatis 中，我们已经有了完善的反射体系：

1. `MetaObject`：提供对象元数据，用于访问对象属性
2. `ObjectWrapper`：包装不同类型的对象，统一属性访问方式
3. `Reflector`：缓存类的反射信息，提高反射性能

在动态 SQL 中，我们将使用这些反射工具来：
1. 访问参数对象的属性值（如 `user.name`）
2. 评估条件表达式（如 `name != null`）
3. 替换 SQL 中的参数占位符（如 `#{name}`）

##### 2.6 实现步骤

1. 创建 `IfSqlNode` 类，实现条件判断逻辑
2. 创建 `TextSqlNode` 类，处理可能包含变量的文本
3. 创建 `DynamicSqlSource` 类，实现动态 SQL 源
4. 创建 `ExpressionEvaluator` 类，实现表达式求值
5. 修改 `XMLScriptBuilder.parseDynamicTags` 方法，识别和处理动态标签
6. 更新 `XMLLanguageDriver`，根据 SQL 是否动态创建不同的 `SqlSource`

##### 2.7 核心类实现示例

###### 2.7.1 ExpressionEvaluator 类

```java
public class ExpressionEvaluator {

    public boolean evaluateBoolean(String expression, Object parameterObject) {
        Object value = OgnlCache.getValue(expression, parameterObject);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof Number) {
            return !new BigDecimal(String.valueOf(value)).equals(BigDecimal.ZERO);
        }
        return value != null;
    }

    public Object evaluateObject(String expression, Object parameterObject) {
        return OgnlCache.getValue(expression, parameterObject);
    }
}
```

###### 2.7.2 OgnlCache 类

```java
public class OgnlCache {

    private static final OgnlMemberAccess MEMBER_ACCESS = new OgnlMemberAccess();
    private static final OgnlClassResolver CLASS_RESOLVER = new OgnlClassResolver();
    private static final Map<String, Object> expressionCache = new ConcurrentHashMap<>();

    public static Object getValue(String expression, Object root) {
        try {
            Map<Object, OgnlClassResolver> context = Ognl.createDefaultContext(root, MEMBER_ACCESS, CLASS_RESOLVER, null);
            return Ognl.getValue(parseExpression(expression), context, root);
        } catch (OgnlException e) {
            throw new RuntimeException("Error evaluating expression '" + expression + "'. Cause: " + e, e);
        }
    }

    private static Object parseExpression(String expression) throws OgnlException {
        Object node = expressionCache.get(expression);
        if (node == null) {
            node = Ognl.parseExpression(expression);
            expressionCache.put(expression, node);
        }
        return node;
    }
}
```

###### 2.7.3 IfSqlNode 类

```java
public class IfSqlNode implements SqlNode {
    private final ExpressionEvaluator evaluator;
    private final String test;
    private final SqlNode contents;

    public IfSqlNode(SqlNode contents, String test) {
        this.test = test;
        this.contents = contents;
        this.evaluator = new ExpressionEvaluator();
    }

    @Override
    public boolean apply(DynamicContext context) {
        if (evaluator.evaluateBoolean(test, context.getBindings().get("_parameter"))) {
            contents.apply(context);
            return true;
        }
        return false;
    }
}
```

###### 2.7.4 TextSqlNode 类

```java
public class TextSqlNode implements SqlNode {
    private final String text;
    private final Pattern pattern = Pattern.compile("\\$\\{(.*?)\\}");

    public TextSqlNode(String text) {
        this.text = text;
    }

    @Override
    public boolean apply(DynamicContext context) {
        GenericTokenParser parser = new GenericTokenParser("${", "}", content -> {
            Object value = OgnlCache.getValue(content, context.getBindings());
            return value == null ? "" : String.valueOf(value);
        });
        context.appendSql(parser.parse(text));
        return true;
    }

    public boolean isDynamic() {
        return pattern.matcher(text).find();
    }
}
```

###### 2.7.5 DynamicSqlSource 类

```java
public class DynamicSqlSource implements SqlSource {
    private final Configuration configuration;
    private final SqlNode rootSqlNode;

    public DynamicSqlSource(Configuration configuration, SqlNode rootSqlNode) {
        this.configuration = configuration;
        this.rootSqlNode = rootSqlNode;
    }

    @Override
    public BoundSql getBoundSql(Object parameterObject) {
        DynamicContext context = new DynamicContext(configuration, parameterObject);
        rootSqlNode.apply(context);
        SqlSourceBuilder sqlSourceParser = new SqlSourceBuilder(configuration);
        SqlSource sqlSource = sqlSourceParser.parse(context.getSql(), parameterObject == null ? Object.class : parameterObject.getClass(), context.getBindings());
        BoundSql boundSql = sqlSource.getBoundSql(parameterObject);
        for (Map.Entry<String, Object> entry : context.getBindings().entrySet()) {
            boundSql.setAdditionalParameter(entry.getKey(), entry.getValue());
        }
        return boundSql;
    }
}
```

###### 2.7.6 修改 XMLScriptBuilder 类

```java
public class XMLScriptBuilder extends BaseBuilder {
    // ... 现有代码 ...

    public SqlSource parseScriptNode() {
        List<SqlNode> contents = parseDynamicTags(element);
        MixedSqlNode rootSqlNode = new MixedSqlNode(contents);

        // 判断是否为动态SQL
        boolean isDynamic = isDynamic;
        if (isDynamic) {
            return new DynamicSqlSource(configuration, rootSqlNode);
        } else {
            return new RawSqlSource(configuration, rootSqlNode, parameterType);
        }
    }

    List<SqlNode> parseDynamicTags(Element element) {
        List<SqlNode> contents = new ArrayList<>();
        // 处理元素的文本内容
        String data = element.getTextTrim();
        if (data != null && data.length() > 0) {
            TextSqlNode textSqlNode = new TextSqlNode(data);
            if (textSqlNode.isDynamic()) {
                contents.add(textSqlNode);
                isDynamic = true;
            } else {
                contents.add(new StaticTextSqlNode(data));
            }
        }

        // 处理子元素
        List<Element> children = element.elements();
        for (Element child : children) {
            String nodeName = child.getName();
            if ("if".equals(nodeName)) {
                String test = child.attributeValue("test");
                List<SqlNode> ifContents = parseDynamicTags(child);
                MixedSqlNode mixedSqlNode = new MixedSqlNode(ifContents);
                contents.add(new IfSqlNode(mixedSqlNode, test));
                isDynamic = true;
            } else {
                // 处理其他类型的标签
                // ...
            }
        }

        return contents;
    }
}
```

##### 2.8 使用示例

###### 2.8.1 XML 配置示例

```xml
<mapper namespace="com.demo.mybatis.UserMapper">
    <select id="selectUser" parameterType="com.demo.mybatis.User">
        SELECT * FROM user
        <if test="name != null">
            WHERE name = #{name}
        </if>
        <if test="age != null">
            <if test="name != null">
                AND
            </if>
            <if test="name == null">
                WHERE
            </if>
            age = #{age}
        </if>
    </select>
</mapper>
```

###### 2.8.2 Java 代码示例

```java
// 创建 SqlSessionFactory
SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);

// 打开 SqlSession
try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
    // 获取 Mapper
    UserMapper userMapper = sqlSession.getMapper(UserMapper.class);

    // 创建查询参数
    User param = new User();
    param.setName("张三");

    // 执行查询
    List<User> users = userMapper.selectUser(param);

    // 处理结果
    for (User user : users) {
        System.out.println(user);
    }
}
```

##### 2.9 反射设计模式与对象值设置的关系

在动态 SQL 的实现中，反射设计模式扮演着核心角色，尤其是在对象值的获取和设置方面。small-mybatis 中的反射机制主要通过以下几个关键类实现：

1. **MetaObject**：对象元数据，提供统一的对象属性访问接口
2. **ObjectWrapper**：对象包装器，处理不同类型对象的属性访问
3. **Reflector**：反射器，缓存类的反射信息，提高性能

这些类共同构成了一个强大的反射体系，使得 MyBatis 能够在不知道具体对象类型的情况下，动态地获取和设置对象属性值。

###### 2.9.1 反射设计模式的应用

在动态 SQL 中，反射设计模式主要应用于以下几个方面：

1. **条件表达式求值**：
   - 当解析 `<if test="user.name != null">` 这样的条件时，需要通过反射获取 `user` 对象的 `name` 属性值
   - `ExpressionEvaluator` 通过 OGNL 和反射机制访问对象属性，评估条件是否为真

2. **参数占位符替换**：
   - 当处理 `#{user.name}` 这样的参数占位符时，需要通过反射获取实际的属性值
   - `ParameterMappingTokenHandler` 使用反射机制获取参数值，并将其替换为 `?` 占位符

3. **动态上下文构建**：
   - `DynamicContext` 使用 `MetaObject` 包装参数对象，提供统一的属性访问方式
   - 通过 `ContextMap` 和反射机制，实现对复杂对象属性的访问

###### 2.9.2 对象值设置的实现

对象值的设置主要通过以下流程实现：

1. **属性路径解析**：
   - `PropertyTokenizer` 解析属性路径，如 `user.address.street`
   - 支持复杂路径，包括嵌套属性、数组索引和 Map 键

2. **反射调用**：
   - `Invoker` 接口及其实现（`MethodInvoker`、`SetFieldInvoker`）封装了反射调用
   - 通过 Java 反射 API 调用 setter 方法或直接设置字段值

3. **类型转换**：
   - `TypeHandler` 体系负责在 Java 类型和 JDBC 类型之间进行转换
   - 确保属性值的类型与目标类型兼容

###### 2.9.3 反射设计模式的优势

在动态 SQL 实现中，反射设计模式带来了以下优势：

1. **灵活性**：无需预先知道对象的具体类型，可以动态处理任何类型的对象
2. **统一接口**：通过 `MetaObject` 和 `ObjectWrapper` 提供统一的属性访问接口
3. **性能优化**：通过 `Reflector` 缓存反射信息，减少反射操作的开销
4. **可扩展性**：可以轻松扩展支持新的对象类型和属性访问方式

##### 2.10 总结

通过实现动态 SQL 功能，small-mybatis 将能够根据参数对象的属性值动态生成 SQL 语句，大大提高了 SQL 的灵活性和可维护性。这一功能的核心是：

1. 使用 `SqlNode` 体系表示 SQL 语句的不同部分
2. 使用 OGNL 表达式语言评估条件
3. 使用反射机制访问参数对象的属性
4. 在运行时动态组装 SQL 语句

这种设计充分利用了 small-mybatis 现有的反射机制，通过组合模式和策略模式实现了灵活的 SQL 生成。反射设计模式在其中扮演着核心角色，使得 MyBatis 能够在不知道具体对象类型的情况下，动态地获取和设置对象属性值，从而实现高度灵活的动态 SQL 功能。
