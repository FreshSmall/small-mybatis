package com.demo.mybatis.scripting.xmltags;

/**
 * If条件SQL节点，用于条件判断
 */
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
        // 使用表达式求值器评估test条件
        if (evaluator.evaluateBoolean(test, context.getBindings().get("_parameter"))) {
            // 如果条件为真，则应用内容
            contents.apply(context);
            return true;
        }
        return false;
    }
}
