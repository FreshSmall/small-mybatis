package com.demo.mybatis.session;

import com.demo.mybatis.builder.xml.XMLConfigBuilder;
import com.demo.mybatis.session.defaults.DefaultSqlSessionFactory;

import java.io.Reader;

/**
 * @author: yinchao
 * @ClassName: SqlSessionFactoryBuilder
 * @Description:
 * @team wuhan operational dev.
 * @date: 2025/5/6 22:46
 */
public class SqlSessionFactoryBuilder {

    public SqlSessionFactory build(Reader reader) {
        XMLConfigBuilder xmlConfigBuilder = new XMLConfigBuilder(reader);
        return build(xmlConfigBuilder.parse());
    }

    public SqlSessionFactory build(Configuration config) {
        return new DefaultSqlSessionFactory(config);
    }
}
