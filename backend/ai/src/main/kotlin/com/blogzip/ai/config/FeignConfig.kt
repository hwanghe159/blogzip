package com.blogzip.ai.config

import feign.Client
import feign.RequestInterceptor
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(OpenAiProperties::class)
@EnableFeignClients("com.blogzip.ai")
class FeignConfig(
    private val openAiProperties: OpenAiProperties,
) {

    @Bean
    fun feignClient(): Client {
        return Client.Default(null, null)
    }

    @Bean
    fun requestInterceptor(): RequestInterceptor {
        return RequestInterceptor {
            it.header("Authorization", "Bearer ${openAiProperties.apiKeyNew}")
        }
    }
}