package com.demo.mybatis.scripting.xmltags;

import java.util.List;

/*
 * @Author: yinchao
 * @Date: 2025-05-19 22:49:49
 * @LastEditors: yinchao
 * @LastEditTime: 2025-05-19 23:35:14
 * @Description: 
 */
public class MixedSqlNode implements SqlNode {
     //组合模式，拥有一个SqlNode的List
     private List<SqlNode> contents;

     public MixedSqlNode(List<SqlNode> contents) {
         this.contents = contents;
     }
 
     @Override
     public boolean apply(DynamicContext context) {
         // 依次调用list里每个元素的apply
         contents.forEach(node -> node.apply(context));
         return true;
     }
}
