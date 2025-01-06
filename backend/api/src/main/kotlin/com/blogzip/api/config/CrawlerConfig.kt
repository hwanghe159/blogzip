package com.blogzip.api.config

import com.blogzip.crawler.service.SeleniumBlogMetadataScrapper
import com.blogzip.crawler.service.RssFeedFetcher
import jakarta.annotation.PreDestroy
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class CrawlerConfig(
    private val seleniumProperties: SeleniumProperties
) {

    private lateinit var seleniumBlogMetadataScrapper: SeleniumBlogMetadataScrapper

    @Bean
    fun blogMetadataScrapper(): SeleniumBlogMetadataScrapper {
        seleniumBlogMetadataScrapper = SeleniumBlogMetadataScrapper.create(
            com.blogzip.crawler.config.SeleniumProperties(seleniumProperties.chromeOptions)
        )
        return seleniumBlogMetadataScrapper
    }

    @PreDestroy
    fun quitWebDriver() {
        seleniumBlogMetadataScrapper.endUse()
    }

    @Bean
    fun rssFeedFetcher(): RssFeedFetcher {
        return RssFeedFetcher.create()
    }
}