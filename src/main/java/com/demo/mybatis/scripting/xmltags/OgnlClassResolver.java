package com.demo.mybatis.scripting.xmltags;

import java.util.HashMap;
import java.util.Map;

import ognl.ClassResolver;

/**
 * OGNL类解析器，用于解析OGNL表达式中的类
 */
public class OgnlClassResolver implements ClassResolver {

    private Map<String, Class<?>> classes = new HashMap<>(101);

    @Override
    public Class<?> classForName(String className, Map context) throws ClassNotFoundException {
        Class<?> result = null;
        if ((result = classes.get(className)) == null) {
            try {
                result = Class.forName(className);
            } catch (ClassNotFoundException e1) {
                if (className.indexOf('.') == -1) {
                    result = Class.forName("java.lang." + className);
                    classes.put("java.lang." + className, result);
                }
            }
            classes.put(className, result);
        }
        return result;
    }
}
