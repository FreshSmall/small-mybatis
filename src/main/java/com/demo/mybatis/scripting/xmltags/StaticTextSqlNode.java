package com.demo.mybatis.scripting.xmltags;

/*
 * @Author: yinchao
 * @Date: 2025-05-19 22:50:16
 * @LastEditors: yinchao
 * @LastEditTime: 2025-05-19 23:35:42
 * @Description: 
 */
public class StaticTextSqlNode implements SqlNode {

    private String text;

    public StaticTextSqlNode(String text) {
        this.text = text;
    }

    @Override
    public boolean apply(DynamicContext context) {
        //将文本加入context
        context.appendSql(text);
        return true;
    }
    
}
