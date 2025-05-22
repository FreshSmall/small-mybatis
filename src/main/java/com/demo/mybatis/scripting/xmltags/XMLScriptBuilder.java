package com.demo.mybatis.scripting.xmltags;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Element;
import org.dom4j.Node;

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

        // 判断是否为动态SQL
        if (isDynamic) {
            return new DynamicSqlSource(configuration, rootSqlNode);
        } else {
            return new RawSqlSource(configuration, rootSqlNode, parameterType);
        }
    }

    List<SqlNode> parseDynamicTags(Element element) {
        List<SqlNode> contents = new ArrayList<>();

        // 处理元素的文本内容和子元素
        List<Node> children = element.content();
        for (Node child : children) {
            if (child.getNodeType() == Node.TEXT_NODE) {
                // 处理文本节点
                String text = child.getText().trim();
                if (text.length() > 0) {
                    TextSqlNode textSqlNode = new TextSqlNode(text);
                    if (textSqlNode.isDynamic()) {
                        contents.add(textSqlNode);
                        isDynamic = true;
                    } else {
                        contents.add(new StaticTextSqlNode(text));
                    }
                }
            } else if (child.getNodeType() == Node.ELEMENT_NODE) {
                // 处理元素节点
                Element childElement = (Element) child;
                String nodeName = childElement.getName();

                // 处理if标签
                if ("if".equals(nodeName)) {
                    String test = childElement.attributeValue("test");
                    List<SqlNode> ifContents = parseDynamicTags(childElement);
                    MixedSqlNode mixedSqlNode = new MixedSqlNode(ifContents);
                    contents.add(new IfSqlNode(mixedSqlNode, test));
                    isDynamic = true;
                } else {
                    // 处理其他标签类型（未来可扩展）
                    // 目前暂不支持其他标签
                }
            }
        }

        return contents;
    }
}
