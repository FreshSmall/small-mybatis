package com.demo.mybatis.scripting.xmltags;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ognl.Ognl;
import ognl.OgnlException;

/**
 * OGNL缓存，用于缓存和执行OGNL表达式
 */
public class OgnlCache {

    private static final OgnlMemberAccess MEMBER_ACCESS = new OgnlMemberAccess();
    private static final OgnlClassResolver CLASS_RESOLVER = new OgnlClassResolver();
    private static final Map<String, Object> expressionCache = new ConcurrentHashMap<>();

    /**
     * 获取表达式的值
     * 
     * @param expression OGNL表达式
     * @param root 根对象
     * @return 表达式的值
     */
    public static Object getValue(String expression, Object root) {
        try {
            Map<Object, OgnlClassResolver> context = Ognl.createDefaultContext(root, MEMBER_ACCESS, CLASS_RESOLVER, null);
            return Ognl.getValue(parseExpression(expression), context, root);
        } catch (OgnlException e) {
            throw new RuntimeException("Error evaluating expression '" + expression + "'. Cause: " + e, e);
        }
    }

    /**
     * 解析表达式，并缓存
     * 
     * @param expression OGNL表达式
     * @return 解析后的表达式对象
     * @throws OgnlException 解析异常
     */
    private static Object parseExpression(String expression) throws OgnlException {
        Object node = expressionCache.get(expression);
        if (node == null) {
            node = Ognl.parseExpression(expression);
            expressionCache.put(expression, node);
        }
        return node;
    }
}
