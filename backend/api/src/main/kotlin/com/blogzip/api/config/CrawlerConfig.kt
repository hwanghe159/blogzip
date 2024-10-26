package com.blogzip.api.config

import com.blogzip.crawler.service.RssFeedFetcher
import com.blogzip.crawler.service.WebScrapper
import jakarta.annotation.PreDestroy
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class CrawlerConfig(
    private val seleniumProperties: SeleniumProperties
) {

    @Bean
    fun webScrapper(): WebScrapper {
        return WebScrapper.create(
            com.blogzip.crawler.config.SeleniumProperties(seleniumProperties.chromeOptions)
        )
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