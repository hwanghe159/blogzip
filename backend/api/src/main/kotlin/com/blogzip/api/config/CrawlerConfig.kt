package com.blogzip.api.config

import com.blogzip.crawler.service.RssFeedFetcher
import com.blogzip.crawler.service.WebScrapper
import jakarta.annotation.PreDestroy
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class CrawlerConfig(
) {

    @Bean
    fun webScrapper(): WebScrapper {
        return WebScrapper.create()
    }

    @PreDestroy
    fun quitWebDriver() {
        webScrapper().endUse()
    }

    @Bean
    fun rssFeedFetcher(): RssFeedFetcher {
        return RssFeedFetcher.create()
    }
}