package com.demo.mybatis.scripting.xmltags;

/*
 * @Author: yinchao
 * @Date: 2025-05-19 22:50:05
 * @LastEditors: yinchao
 * @LastEditTime: 2025-05-19 23:34:56
 * @Description: 
 */
public interface SqlNode {

    boolean apply(DynamicContext context);
}
