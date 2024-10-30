package com.blogzip.batch.config

import com.blogzip.crawler.service.ChromeWebScrapper
import com.blogzip.crawler.service.HtmlCompressor
import com.blogzip.crawler.service.RssFeedFetcher
import com.blogzip.crawler.service.WebScrapper
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class CrawlerConfig(
    private val seleniumProperties: SeleniumProperties
) {

    @StepScope
    @Bean
    fun chromeWebScrapper(): WebScrapper {
        return ChromeWebScrapper.create(
            com.blogzip.crawler.config.SeleniumProperties(seleniumProperties.chromeOptions)
        )
    }

    @Bean
    fun rssFeedFetcher(): RssFeedFetcher {
        return RssFeedFetcher.create()
    }

    @Bean
    fun htmlCompressor(): HtmlCompressor {
        return HtmlCompressor()
    }
}