package com.demo.mybatis.binding;

import com.demo.mybatis.annotations.Delete;
import com.demo.mybatis.annotations.Insert;
import com.demo.mybatis.annotations.Select;
import com.demo.mybatis.annotations.Update;
import com.demo.mybatis.mapping.MappedStatement;
import com.demo.mybatis.mapping.SqlCommandType;
import com.demo.mybatis.session.Configuration;
import com.demo.mybatis.session.SqlSession;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author: yinchao
 * @ClassName: MapperMethod
 * @Description:
 * @team wuhan operational dev.
 * @date: 2025/5/6 22:43
 */
public class MapperMethod {

    private final SqlCommand command;
    private final MethodSignature method;


    public MapperMethod(Class<?> mapperInterface, Method method, Configuration configuration) {
        this.command = new SqlCommand(configuration, mapperInterface, method);
        this.method = new MethodSignature(configuration, method);
    }

    public Object execute(SqlSession sqlSession, Object[] args) {
        Object result = null;
        switch (command.getType()) {
            case INSERT:
                Object param = method.convertArgsToSqlCommandParam(args);
                result = sqlSession.insert(command.getName(), param);
                sqlSession.commit();
                break;
            case DELETE:
                param = method.convertArgsToSqlCommandParam(args);
                result = sqlSession.delete(command.getName(), param);
                sqlSession.commit();
                break;
            case UPDATE:
                param = method.convertArgsToSqlCommandParam(args);
                result = sqlSession.update(command.getName(), param);
                sqlSession.commit();
                break;
            case SELECT:
                param = method.convertArgsToSqlCommandParam(args);
                if (method.returnsMany()) {
                    result = sqlSession.selectList(command.getName(), param);
                } else {
                    result = sqlSession.selectOne(command.getName(), param);
                }
                break;
            default:
                throw new RuntimeException("Unknown execution method for: " + command.getName());
        }
        return result;
    }

    /**
     * SQL 指令
     */
    public static class SqlCommand {

        private final String name;
        private final SqlCommandType type;

        public SqlCommand(Configuration configuration, Class<?> mapperInterface, Method method) {
            String statementName = mapperInterface.getName() + "." + method.getName();

            // 检查配置中是否已经有这个语句
            if (configuration.hasStatement(statementName)) {
                MappedStatement ms = configuration.getMappedStatement(statementName);
                name = ms.getId();
                type = ms.getSqlCommandType();
            } else {
                // 检查方法上是否有 SQL 注解
                if (method.isAnnotationPresent(Select.class)) {
                    name = statementName;
                    type = SqlCommandType.SELECT;
                } else if (method.isAnnotationPresent(Insert.class)) {
                    name = statementName;
                    type = SqlCommandType.INSERT;
                } else if (method.isAnnotationPresent(Update.class)) {
                    name = statementName;
                    type = SqlCommandType.UPDATE;
                } else if (method.isAnnotationPresent(Delete.class)) {
                    name = statementName;
                    type = SqlCommandType.DELETE;
                } else {
                    name = statementName;
                    type = SqlCommandType.UNKNOWN;
                }
            }
        }

        public String getName() {
            return name;
        }

        public SqlCommandType getType() {
            return type;
        }
    }
    /**
     * 方法签名
     */
    public static class MethodSignature {

        private final SortedMap<Integer, String> params;
        private final Class<?> returnType;

        public MethodSignature(Configuration configuration, Method method) {
            this.params = Collections.unmodifiableSortedMap(getParams(method));
            this.returnType = method.getReturnType();
        }

        public Object convertArgsToSqlCommandParam(Object[] args) {
            final int paramCount = params.size();
            if (args == null || paramCount == 0) {
                //如果没参数
                return null;
            } else if (paramCount == 1) {
                return args[params.keySet().iterator().next().intValue()];
            } else {
                // 否则，返回一个ParamMap，修改参数名，参数名就是其位置
                final Map<String, Object> param = new ParamMap<Object>();
                int i = 0;
                for (Map.Entry<Integer, String> entry : params.entrySet()) {
                    // 1.先加一个#{0},#{1},#{2}...参数
                    param.put(entry.getValue(), args[entry.getKey().intValue()]);
                    // issue #71, add param names as param1, param2...but ensure backward compatibility
                    final String genericParamName = "param" + (i + 1);
                    if (!param.containsKey(genericParamName)) {
                        /*
                         * 2.再加一个#{param1},#{param2}...参数
                         * 你可以传递多个参数给一个映射器方法。如果你这样做了,
                         * 默认情况下它们将会以它们在参数列表中的位置来命名,比如:#{param1},#{param2}等。
                         * 如果你想改变参数的名称(只在多参数情况下) ,那么你可以在参数上使用@Param(“paramName”)注解。
                         */
                        param.put(genericParamName, args[entry.getKey()]);
                    }
                    i++;
                }
                return param;
            }
        }

        public boolean returnsMany() {
            return java.util.Collection.class.isAssignableFrom(returnType) || returnType.isArray();
        }

        private SortedMap<Integer, String> getParams(Method method) {
            // 用一个TreeMap，这样就保证还是按参数的先后顺序
            final SortedMap<Integer, String> params = new TreeMap<Integer, String>();
            final Class<?>[] argTypes = method.getParameterTypes();
            final java.lang.reflect.Parameter[] parameters = method.getParameters();

            for (int i = 0; i < argTypes.length; i++) {
                String paramName;

                // 检查参数是否有 @Param 注解
                if (parameters[i].isAnnotationPresent(com.demo.mybatis.annotations.Param.class)) {
                    paramName = parameters[i].getAnnotation(com.demo.mybatis.annotations.Param.class).value();
                } else {
                    paramName = String.valueOf(argTypes[i]);
                }

                params.put(i, paramName);
            }
            return params;
        }

    }


    /**
     * 参数map，静态内部类,更严格的get方法，如果没有相应的key，报错
     */
    public static class ParamMap<V> extends HashMap<String, V> {

        private static final long serialVersionUID = -2212268410512043556L;

        @Override
        public V get(Object key) {
            if (!super.containsKey(key)) {
                throw new RuntimeException("Parameter '" + key + "' not found. Available parameters are " + keySet());
            }
            return super.get(key);
        }

    }

}
