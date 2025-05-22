package com.demo.mybatis.builder.xml;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.demo.mybatis.builder.BaseBuilder;
import com.demo.mybatis.io.Resources;
import com.demo.mybatis.mapping.ResultMap;
import com.demo.mybatis.mapping.ResultMapping;
import com.demo.mybatis.session.Configuration;
import com.demo.mybatis.type.JdbcType;

/**
 * @author: yinchao
 * @ClassName: XMLMapperBuilder
 * @Description:
 * @team wuhan operational dev.
 * @date: 2025/5/19 10:40
 */
public class XMLMapperBuilder extends BaseBuilder {

    private Element element;
    private String resource;
    private String currentNamespace;

    public XMLMapperBuilder(InputStream inputStream, Configuration configuration, String resource) throws DocumentException {
        this(new SAXReader().read(inputStream), configuration, resource);
    }

    private XMLMapperBuilder(Document document, Configuration configuration, String resource) {
        super(configuration);
        this.element = document.getRootElement();
        this.resource = resource;
    }

    /**
     * 解析
     */
    public void parse() throws Exception {
        // 如果当前资源没有加载过再加载，防止重复加载
        if (!configuration.isResourceLoaded(resource)) {
            configurationElement(element);
            // 标记一下，已经加载过了
            configuration.addLoadedResource(resource);
            // 绑定映射器到namespace
            Class<?> mapperInterface = Resources.classForName(currentNamespace);
            if (!configuration.hasMapper(mapperInterface)) {
                configuration.addMapper(mapperInterface);
            }
        }
    }

    // 配置mapper元素
    // <mapper namespace="org.mybatis.example.BlogMapper">
    //   <select id="selectBlog" parameterType="int" resultType="Blog">
    //    select * from Blog where id = #{id}
    //   </select>
    // </mapper>
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

    // 配置resultMap
    private void buildResultMapElements(List<Element> list) {
        for (Element element : list) {
            try {
                resultMapElement(element);
            } catch (Exception e) {
                throw new RuntimeException("Error parsing resultMap element: " + e, e);
            }
        }
    }

    // 配置resultMap
    private void resultMapElement(Element resultMapNode) throws Exception {
        String id = resultMapNode.attributeValue("id");
        String type = resultMapNode.attributeValue("type");
        Class<?> typeClass = resolveAlias(type);

        List<ResultMapping> resultMappings = new ArrayList<>();

        // 解析 id 元素
        List<Element> idChildren = resultMapNode.elements("id");
        for (Element idChild : idChildren) {
            ResultMapping resultMapping = buildResultMapping(idChild, typeClass);
            resultMappings.add(resultMapping);
        }

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

    // 构建ResultMapping
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

    // 解析类
    private Class<?> resolveClass(String alias) {
        if (alias == null) {
            return null;
        }
        try {
            return resolveAlias(alias);
        } catch (Exception e) {
            throw new RuntimeException("Error resolving class. Cause: " + e, e);
        }
    }

    // 解析JDBC类型
    private JdbcType resolveJdbcType(String alias) {
        if (alias == null) {
            return null;
        }
        try {
            return JdbcType.valueOf(alias.toUpperCase(Locale.ENGLISH));
        } catch (Exception e) {
            throw new RuntimeException("Error resolving JDBC type. Cause: " + e, e);
        }
    }

    // 配置select|insert|update|delete
    private void buildStatementFromContext(List<Element> list) {
        for (Element element : list) {
            final XMLStatementBuilder statementParser = new XMLStatementBuilder(configuration, element, currentNamespace);
            statementParser.parseStatementNode();
        }
    }
}