package com.demo.mybatis.scripting.xmltags;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Element;

import com.demo.mybatis.builder.BaseBuilder;
import com.demo.mybatis.mapping.SqlSource;
import com.demo.mybatis.scripting.defaults.RawSqlSource;
import com.demo.mybatis.session.Configuration;

/*
 * @Author: yinchao
 * @Date: 2025-05-19 22:50:36
 * @LastEditors: yinchao
 * @LastEditTime: 2025-05-19 22:50:37
 * @Description: 
 */
public class XMLScriptBuilder extends BaseBuilder {

    private Element element;
    private boolean isDynamic;
    private Class<?> parameterType;

    public XMLScriptBuilder(Configuration configuration, Element element, Class<?> parameterType) {
        super(configuration);
        this.element = element;
        this.parameterType = parameterType;
    }

    public SqlSource parseScriptNode() {
        List<SqlNode> contents = parseDynamicTags(element);
        MixedSqlNode rootSqlNode = new MixedSqlNode(contents);
        return new RawSqlSource(configuration, rootSqlNode, parameterType);
    }

    List<SqlNode> parseDynamicTags(Element element) {
        List<SqlNode> contents = new ArrayList<>();
        // element.getText 拿到 SQL
        String data = element.getText();
        contents.add(new StaticTextSqlNode(data));
        return contents;
    }
}
