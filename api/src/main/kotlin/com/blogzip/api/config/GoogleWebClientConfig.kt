package com.blogzip.api.config

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
@EnableConfigurationProperties(OAuth2Properties::class)
class GoogleWebClientConfig(
    private val oAuth2Properties: OAuth2Properties
) {
    @Bean
    fun googleAuthWebClient(): WebClient {
        return WebClient.create("https://oauth2.googleapis.com")
    }
}