package com.demo.mybatis.scripting.xmltags;

import java.math.BigDecimal;

/**
 * 表达式求值器，用于评估OGNL表达式
 */
public class ExpressionEvaluator {

    /**
     * 评估表达式是否为真
     * 
     * @param expression OGNL表达式
     * @param parameterObject 参数对象
     * @return 表达式的布尔值
     */
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

    /**
     * 计算表达式的值
     * 
     * @param expression OGNL表达式
     * @param parameterObject 参数对象
     * @return 表达式的值
     */
    public Object evaluateObject(String expression, Object parameterObject) {
        return OgnlCache.getValue(expression, parameterObject);
    }
}
