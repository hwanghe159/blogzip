package com.blogzip.ai.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import feign.Client
import feign.RequestInterceptor
import feign.codec.Decoder
import feign.codec.Encoder
import org.springframework.beans.factory.ObjectFactory
import org.springframework.boot.autoconfigure.http.HttpMessageConverters
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.cloud.openfeign.support.SpringDecoder
import org.springframework.cloud.openfeign.support.SpringEncoder
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

    @Bean
    fun feignEncoder(): Encoder {
        val messageConverters: ObjectFactory<HttpMessageConverters> =
            ObjectFactory<HttpMessageConverters> {
                HttpMessageConverters()
            }
        return SpringEncoder(messageConverters)
    }

    @Bean
    fun feignDecoder(): Decoder {
        val messageConverters: ObjectFactory<HttpMessageConverters> =
            ObjectFactory<HttpMessageConverters> {
                HttpMessageConverters()
            }
        return SpringDecoder(messageConverters)
    }

    @Bean
    fun snakeCaseObjectMapper(): ObjectMapper {
        return jsonMapper { addModule(kotlinModule()) }
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
    }

    @Bean
    fun feignLoggerLevel(): feign.Logger.Level {
        return feign.Logger.Level.FULL
    }
}