package com.blogzip.api.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.http.codec.json.Jackson2JsonEncoder
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class GoogleWebClientConfig {

    @Bean
    fun googleAuthWebClient(): WebClient {
        return WebClient.builder()
            .baseUrl("https://oauth2.googleapis.com")
            .exchangeStrategies(commonExchangeStrategies())
            .build()
    }

    @Bean
    fun googleWebClient(): WebClient {
        return WebClient.builder()
            .baseUrl("https://www.googleapis.com")
            .exchangeStrategies(commonExchangeStrategies())
            .build()
    }

    private fun commonExchangeStrategies(): ExchangeStrategies {
        val objectMapper: ObjectMapper = Jackson2ObjectMapperBuilder.json()
            .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
            .build()

        return ExchangeStrategies.builder()
            .codecs { clientCodecConfigurer ->
                clientCodecConfigurer.defaultCodecs()
                    .jackson2JsonDecoder(Jackson2JsonDecoder(objectMapper))
                clientCodecConfigurer.defaultCodecs()
                    .jackson2JsonEncoder(Jackson2JsonEncoder(objectMapper))
            }
            .build()
    }
}