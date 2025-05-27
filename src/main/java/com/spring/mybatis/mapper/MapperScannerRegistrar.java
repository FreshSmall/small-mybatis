package com.spring.mybatis.mapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.StringUtils;

/**
 * @author: yinchao
 * @ClassName: MapperScannerRegistrar
 * @Description: Mapper扫描注册器，处理@MapperScan注解并注册MapperScannerConfigurer
 * 实现ImportBeanDefinitionRegistrar接口，在Spring容器启动时执行
 * @team wuhan operational dev.
 * @date: 2025/5/6 14:30
 */
public class MapperScannerRegistrar implements ImportBeanDefinitionRegistrar {

    private static final Logger logger = LoggerFactory.getLogger(MapperScannerRegistrar.class);

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        AnnotationAttributes annoAttrs = AnnotationAttributes.fromMap(
            importingClassMetadata.getAnnotationAttributes(MapperScan.class.getName()));
        
        if (annoAttrs != null) {
            registerMapperScannerConfigurer(registry, annoAttrs);
        }
    }

    /**
     * 注册MapperScannerConfigurer Bean定义
     *
     * @param registry Bean定义注册表
     * @param annoAttrs 注解属性
     */
    private void registerMapperScannerConfigurer(BeanDefinitionRegistry registry, AnnotationAttributes annoAttrs) {
        GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
        beanDefinition.setBeanClass(MapperScannerConfigurer.class);

        // 获取要扫描的包名
        List<String> basePackages = new ArrayList<>();
        basePackages.addAll(Arrays.asList(annoAttrs.getStringArray("value")));
        basePackages.addAll(Arrays.asList(annoAttrs.getStringArray("basePackages")));

        if (basePackages.isEmpty()) {
            throw new IllegalArgumentException("At least one base package must be specified");
        }

        // 设置属性
        beanDefinition.getPropertyValues().add("basePackage", StringUtils.collectionToCommaDelimitedString(basePackages));

        String sqlSessionFactoryRef = annoAttrs.getString("sqlSessionFactoryRef");
        if (StringUtils.hasText(sqlSessionFactoryRef)) {
            beanDefinition.getPropertyValues().add("sqlSessionFactoryBeanName", sqlSessionFactoryRef);
        }

        String sqlSessionTemplateRef = annoAttrs.getString("sqlSessionTemplateRef");
        if (StringUtils.hasText(sqlSessionTemplateRef)) {
            beanDefinition.getPropertyValues().add("sqlSessionTemplateBeanName", sqlSessionTemplateRef);
        }

        // 注册Bean定义
        String beanName = "mapperScannerConfigurer#" + System.identityHashCode(beanDefinition);
        registry.registerBeanDefinition(beanName, beanDefinition);

        logger.debug("Registered MapperScannerConfigurer with base packages: {}", basePackages);
    }
} 