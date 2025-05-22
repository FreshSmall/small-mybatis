package com.demo.mybatis.scripting.xmltags;

import java.util.regex.Pattern;

import com.demo.mybatis.parsing.GenericTokenParser;
import com.demo.mybatis.parsing.TokenHandler;

/**
 * 文本SQL节点，可能包含${xxx}变量的文本
 */
public class TextSqlNode implements SqlNode {
    private final String text;
    private final Pattern pattern = Pattern.compile("\\$\\{(.*?)\\}");

    public TextSqlNode(String text) {
        this.text = text;
    }

    @Override
    public boolean apply(DynamicContext context) {
        // 使用GenericTokenParser处理${}变量
        GenericTokenParser parser = new GenericTokenParser("${", "}", new BindingTokenParser(context));
        context.appendSql(parser.parse(text));
        return true;
    }

    /**
     * 判断是否为动态SQL
     * 
     * @return 如果包含${xxx}变量，则返回true
     */
    public boolean isDynamic() {
        return pattern.matcher(text).find();
    }

    /**
     * 绑定标记解析器，用于解析${xxx}变量
     */
    private static class BindingTokenParser implements TokenHandler {
        private DynamicContext context;

        public BindingTokenParser(DynamicContext context) {
            this.context = context;
        }

        @Override
        public String handleToken(String content) {
            Object parameter = context.getBindings().get("_parameter");
            if (parameter == null) {
                context.getBindings().get("_parameter");
            }
            Object value = OgnlCache.getValue(content, context.getBindings());
            return value == null ? "" : String.valueOf(value);
        }
    }
}
