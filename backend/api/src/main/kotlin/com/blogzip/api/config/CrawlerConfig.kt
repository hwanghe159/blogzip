package com.blogzip.api.config

import com.blogzip.crawler.service.RssFeedFetcher
import com.blogzip.crawler.service.ChromeWebScrapper
import com.blogzip.crawler.service.WebScrapper
import jakarta.annotation.PreDestroy
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class CrawlerConfig(
    private val seleniumProperties: SeleniumProperties
) {

    @Bean
    fun chromeWebScrapper(): WebScrapper {
        return ChromeWebScrapper.create(
            com.blogzip.crawler.config.SeleniumProperties(seleniumProperties.chromeOptions)
        )
    }

    @PreDestroy
    fun quitWebDriver() {
        chromeWebScrapper().endUse()
    }

    @Bean
    fun rssFeedFetcher(): RssFeedFetcher {
        return RssFeedFetcher.create()
    }
}