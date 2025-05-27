package com.spring.mybatis.mapper;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

/**
 * @author: yinchao
 * @ClassName: MapperScan
 * @Description: Mapper扫描注解，用于自动扫描和注册Mapper接口
 * 使用此注解可以替代XML配置方式
 * @team wuhan operational dev.
 * @date: 2025/5/6 14:00
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(MapperScannerRegistrar.class)
public @interface MapperScan {

    /**
     * 要扫描的基础包名
     *
     * @return 基础包名数组
     */
    String[] value() default {};

    /**
     * 要扫描的基础包名（与value相同）
     *
     * @return 基础包名数组
     */
    String[] basePackages() default {};

    /**
     * SqlSessionFactory Bean名称
     *
     * @return SqlSessionFactory Bean名称
     */
    String sqlSessionFactoryRef() default "sqlSessionFactory";

    /**
     * SqlSessionTemplate Bean名称
     *
     * @return SqlSessionTemplate Bean名称
     */
    String sqlSessionTemplateRef() default "";
} 