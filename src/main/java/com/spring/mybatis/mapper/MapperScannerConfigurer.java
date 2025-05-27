package com.spring.mybatis.mapper;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

/**
 * @author: yinchao
 * @ClassName: MapperScannerConfigurer
 * @Description: Mapper扫描配置器，自动扫描指定包下的Mapper接口并注册为Spring Bean
 * 实现BeanFactoryPostProcessor接口，在Bean定义加载完成后执行扫描
 * @team wuhan operational dev.
 * @date: 2025/5/6 11:30
 */
public class MapperScannerConfigurer implements BeanFactoryPostProcessor, ResourceLoaderAware {

    private static final Logger logger = LoggerFactory.getLogger(MapperScannerConfigurer.class);

    /**
     * 要扫描的基础包
     */
    private String basePackage;

    /**
     * SqlSessionFactory Bean名称
     */
    private String sqlSessionFactoryBeanName = "sqlSessionFactory";

    /**
     * SqlSessionTemplate Bean名称
     */
    private String sqlSessionTemplateBeanName;

    /**
     * 资源加载器
     */
    private ResourceLoader resourceLoader;

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        if (!StringUtils.hasText(this.basePackage)) {
            logger.warn("No base package specified for MapperScannerConfigurer");
            return;
        }

        if (!(beanFactory instanceof BeanDefinitionRegistry)) {
            throw new IllegalStateException("BeanFactory must be a BeanDefinitionRegistry");
        }

        BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
        
        try {
            scanAndRegisterMappers(registry);
        } catch (Exception e) {
            throw new RuntimeException("Failed to scan mappers", e);
        }
    }

    /**
     * 扫描并注册Mapper接口
     *
     * @param registry Bean定义注册表
     * @throws Exception 扫描异常
     */
    private void scanAndRegisterMappers(BeanDefinitionRegistry registry) throws Exception {
        // 构建扫描路径
        String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX +
            ClassUtils.convertClassNameToResourcePath(this.basePackage) + "/**/*.class";

        ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver(this.resourceLoader);
        MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(this.resourceLoader);

        Resource[] resources = resourcePatternResolver.getResources(packageSearchPath);
        
        for (Resource resource : resources) {
            if (resource.isReadable()) {
                try {
                    MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(resource);
                    String className = metadataReader.getClassMetadata().getClassName();
                    
                    // 检查是否是接口
                    if (metadataReader.getClassMetadata().isInterface()) {
                        Class<?> mapperClass = Class.forName(className);
                        
                        // 检查是否是Mapper接口（这里可以添加更多过滤条件）
                        if (isMapperInterface(mapperClass)) {
                            registerMapperBean(registry, mapperClass);
                            logger.debug("Registered mapper: {}", className);
                        }
                    }
                } catch (IOException | ClassNotFoundException e) {
                    logger.warn("Failed to process resource: " + resource, e);
                }
            }
        }
    }

    /**
     * 检查是否是Mapper接口
     * 
     * @param clazz 类对象
     * @return 是否是Mapper接口
     */
    private boolean isMapperInterface(Class<?> clazz) {
        // 检查是否是接口
        if (!clazz.isInterface()) {
            return false;
        }
        
        // 排除系统接口和注解
        String className = clazz.getName();
        
        // 排除注解类
        if (clazz.isAnnotation()) {
            return false;
        }
        
        // 排除系统接口
        if (className.contains(".annotations.") ||
            className.contains(".cache.") ||
            className.contains(".datasource.") ||
            className.contains(".executor.") ||
            className.contains(".mapping.") ||
            className.contains(".parsing.") ||
            className.contains(".plugin.") ||
            className.contains(".reflection.") ||
            className.contains(".scripting.") ||
            className.contains(".session.") ||
            className.contains(".transaction.") ||
            className.contains(".type.")) {
            return false;
        }
        
        // 只扫描用户定义的Mapper接口
        return true;
    }

    /**
     * 注册Mapper Bean定义
     *
     * @param registry Bean定义注册表
     * @param mapperClass Mapper接口类
     */
    private void registerMapperBean(BeanDefinitionRegistry registry, Class<?> mapperClass) {
        GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
        beanDefinition.setBeanClass(MapperFactoryBean.class);
        beanDefinition.setScope(BeanDefinition.SCOPE_SINGLETON);
        
        // 设置构造函数参数：Mapper接口类
        beanDefinition.getConstructorArgumentValues().addGenericArgumentValue(mapperClass);
        
        // 设置属性：SqlSessionFactory或SqlSessionTemplate
        if (StringUtils.hasText(this.sqlSessionTemplateBeanName)) {
            beanDefinition.getPropertyValues().add("sqlSessionTemplate", 
                new org.springframework.beans.factory.config.RuntimeBeanReference(this.sqlSessionTemplateBeanName));
        } else if (StringUtils.hasText(this.sqlSessionFactoryBeanName)) {
            beanDefinition.getPropertyValues().add("sqlSessionFactory", 
                new org.springframework.beans.factory.config.RuntimeBeanReference(this.sqlSessionFactoryBeanName));
        }

        // 使用接口名作为Bean名称（首字母小写）
        String beanName = StringUtils.uncapitalize(mapperClass.getSimpleName());
        registry.registerBeanDefinition(beanName, beanDefinition);
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    /**
     * 设置要扫描的基础包
     *
     * @param basePackage 基础包名
     */
    public void setBasePackage(String basePackage) {
        this.basePackage = basePackage;
    }

    /**
     * 设置SqlSessionFactory Bean名称
     *
     * @param sqlSessionFactoryBeanName SqlSessionFactory Bean名称
     */
    public void setSqlSessionFactoryBeanName(String sqlSessionFactoryBeanName) {
        this.sqlSessionFactoryBeanName = sqlSessionFactoryBeanName;
    }

    /**
     * 设置SqlSessionTemplate Bean名称
     *
     * @param sqlSessionTemplateBeanName SqlSessionTemplate Bean名称
     */
    public void setSqlSessionTemplateBeanName(String sqlSessionTemplateBeanName) {
        this.sqlSessionTemplateBeanName = sqlSessionTemplateBeanName;
    }

    /**
     * 获取基础包名
     *
     * @return 基础包名
     */
    public String getBasePackage() {
        return basePackage;
    }

    /**
     * 获取SqlSessionFactory Bean名称
     *
     * @return SqlSessionFactory Bean名称
     */
    public String getSqlSessionFactoryBeanName() {
        return sqlSessionFactoryBeanName;
    }

    /**
     * 获取SqlSessionTemplate Bean名称
     *
     * @return SqlSessionTemplate Bean名称
     */
    public String getSqlSessionTemplateBeanName() {
        return sqlSessionTemplateBeanName;
    }
} 