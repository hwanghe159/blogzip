package com.blogzip.crawler.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class WebClientConfig {

    @Bean
    fun xmlWebClient(): WebClient {
        return WebClient.builder()
            .defaultHeaders { headers ->
                headers.setAll(
                    mapOf(
                        HttpHeaders.CONTENT_TYPE to MediaType.APPLICATION_RSS_XML_VALUE,
                        HttpHeaders.USER_AGENT to "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.0.0 Safari/537.36",
                    )
                )
            }
            .exchangeStrategies(
                ExchangeStrategies.builder()
                    .codecs { it.defaultCodecs().maxInMemorySize(-1) }
                    .build()
            ).build()
    }

    @Bean
    fun defaultWebClient(): WebClient {
        return WebClient.builder()
            .exchangeStrategies(
                ExchangeStrategies.builder()
                    .codecs { it.defaultCodecs().maxInMemorySize(-1) }
                    .build()
            ).build()
    }
}