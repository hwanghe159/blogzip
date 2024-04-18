package com.blogzip.batch.config

import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class BatchConfig {

    @Bean
    fun jobRegistryBeanPostProcessorRemover(): BeanDefinitionRegistryPostProcessor {
        return BeanDefinitionRegistryPostProcessor { registry ->
            registry.removeBeanDefinition("jobRegistryBeanPostProcessor")
        }
    }
}