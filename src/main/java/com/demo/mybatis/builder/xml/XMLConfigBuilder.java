package com.demo.mybatis.builder.xml;

import java.io.InputStream;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.xml.sax.InputSource;

import com.demo.mybatis.builder.BaseBuilder;
import com.demo.mybatis.builder.StaticSqlSource;
import com.demo.mybatis.datasource.DataSourceFactory;
import com.demo.mybatis.io.Resources;
import com.demo.mybatis.mapping.Environment;
import com.demo.mybatis.mapping.MappedStatement;
import com.demo.mybatis.mapping.SqlCommandType;
import com.demo.mybatis.mapping.SqlSource;
import com.demo.mybatis.plugin.Interceptor;
import com.demo.mybatis.session.Configuration;
import com.demo.mybatis.transaction.TransactionFactory;

/**
 * @author: yinchao
 * @ClassName: XMLConfigBuilder
 * @Description:
 * @team wuhan operational dev.
 * @date: 2025/5/6 22:44
 */
public class XMLConfigBuilder extends BaseBuilder {

    private Element root;

    public XMLConfigBuilder(Reader reader) {
        // 1. 调用父类初始化Configuration
        super(new Configuration());
        // 2. dom4j 处理 xml
        SAXReader saxReader = new SAXReader();
        try {
            Document document = saxReader.read(new InputSource(reader));
            root = document.getRootElement();
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    /**
     * 解析配置；类型别名、插件、对象工厂、对象包装工厂、设置、环境、类型转换、映射器
     *
     * @return Configuration
     */
    public Configuration parse() {
        try {
            // 解析插件
            pluginElement(root.element("plugins"));
            // 解析环境
            environmentElement(root.element("environments"));
            // 解析映射器
            mapperElement(root.element("mappers"));
        } catch (Exception e) {
            throw new RuntimeException("Error parsing SQL Mapper Configuration. Cause: " + e, e);
        }
        return configuration;
    }

    private void environmentElement(Element context) throws Exception {
        String environment = context.attributeValue("default");

        List<Element> environmentList = context.elements("environment");
        for (Element e : environmentList) {
            String id = e.attributeValue("id");
            if (environment.equals(id)) {
                // 事务管理器
                TransactionFactory txFactory = (TransactionFactory) typeAliasRegistry.resolveAlias(e.element("transactionManager").attributeValue("type")).newInstance();
                // 数据源
                Element dataSourceElement = e.element("dataSource");
                DataSourceFactory dataSourceFactory = (DataSourceFactory) typeAliasRegistry.resolveAlias(dataSourceElement.attributeValue("type")).newInstance();
                List<Element> propertyList = dataSourceElement.elements("property");
                Properties props = new Properties();
                for (Element property : propertyList) {
                    props.setProperty(property.attributeValue("name"), property.attributeValue("value"));
                }
                dataSourceFactory.setProperties(props);
                DataSource dataSource = dataSourceFactory.getDataSource();
                // 构建环境
                Environment.Builder environmentBuilder = new Environment.Builder(id)
                        .transactionFactory(txFactory)
                        .dataSource(dataSource);
                configuration.setEnvironment(environmentBuilder.build());
            }
        }
    }

    /**
     * 解析插件标签
     */
    private void pluginElement(Element parent) throws Exception {
        if (parent != null) {
            for (Element child : parent.elements("plugin")) {
                String interceptor = child.attributeValue("interceptor");
                Properties properties = new Properties();
                // 解析属性配置
                for (Element property : child.elements("property")) {
                    String name = property.attributeValue("name");
                    String value = property.attributeValue("value");
                    properties.setProperty(name, value);
                }

                // 实例化拦截器
                Interceptor interceptorInstance = (Interceptor) resolveClass(interceptor).newInstance();
                // 设置属性
                interceptorInstance.setProperties(properties);
                // 添加到配置中
                configuration.addInterceptor(interceptorInstance);
            }
        }
    }

    private void mapperElement(Element mappers) throws Exception {
        List<Element> mapperList = mappers.elements("mapper");
        for (Element e : mapperList) {
            String resource = e.attributeValue("resource");
            String mapperClass = e.attributeValue("class");

            if (resource != null && mapperClass == null) {
                // 使用 XML 配置
                InputStream inputStream = Resources.getResourceAsStream(resource);
                // 在for循环里每个mapper都重新new一个XMLMapperBuilder，来解析
                XMLMapperBuilder mapperParser = new XMLMapperBuilder(inputStream, configuration, resource);
                mapperParser.parse();
            } else if (resource == null && mapperClass != null) {
                // 使用注解配置
                Class<?> mapperInterface = Resources.classForName(mapperClass);
                configuration.addMapper(mapperInterface);
            } else {
                throw new RuntimeException("A mapper element must specify either a resource or a class, but not both.");
            }
        }
    }

    /**
     * 解析类
     */
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

}
