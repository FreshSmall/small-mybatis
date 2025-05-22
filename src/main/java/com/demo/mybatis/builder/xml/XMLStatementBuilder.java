package com.demo.mybatis.builder.xml;

import java.util.Locale;

import org.dom4j.Element;

import com.demo.mybatis.builder.BaseBuilder;
import com.demo.mybatis.mapping.MappedStatement;
import com.demo.mybatis.mapping.ResultMap;
import com.demo.mybatis.mapping.SqlCommandType;
import com.demo.mybatis.mapping.SqlSource;
import com.demo.mybatis.scripting.LanguageDriver;
import com.demo.mybatis.session.Configuration;

/*
 * @Author: yinchao ycsuper2819@gmail.com
 * @Date: 2025-05-19 22:44:09
 * @LastEditors: yinchao
 * @LastEditTime: 2025-05-19 23:27:33
 * @Description:
 */
public class XMLStatementBuilder extends BaseBuilder {

    private String currentNamespace;
    private Element element;

    public XMLStatementBuilder(Configuration configuration, Element element, String currentNamespace) {
        super(configuration);
        this.element = element;
        this.currentNamespace = currentNamespace;
    }

    //解析语句(select|insert|update|delete)
    //<select
    //  id="selectPerson"
    //  parameterType="int"
    //  parameterMap="deprecated"
    //  resultType="hashmap"
    //  resultMap="personResultMap"
    //  flushCache="false"
    //  useCache="true"
    //  timeout="10000"
    //  fetchSize="256"
    //  statementType="PREPARED"
    //  resultSetType="FORWARD_ONLY">
    //  SELECT * FROM PERSON WHERE ID = #{id}
    //</select>
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
            String fullResultMapId = currentNamespace + "." + resultMap;
            statementBuilder.resultMap(fullResultMapId);

            // 如果使用了resultMap，需要从resultMap中获取对应的类型
            ResultMap rm = configuration.getResultMap(fullResultMapId);
            if (rm != null) {
                // 使用resultMap中的类型覆盖resultType
                statementBuilder = new MappedStatement.Builder(configuration, currentNamespace + "." + id, sqlCommandType, sqlSource, rm.getType());
                statementBuilder.resultMap(fullResultMapId);
            }
        }

        MappedStatement mappedStatement = statementBuilder.build();
        // 添加解析 SQL
        configuration.addMappedStatement(mappedStatement);
    }

}
