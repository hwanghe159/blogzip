package com.blogzip.batch.config

import com.blogzip.crawler.service.HtmlCompressor
import com.blogzip.crawler.service.RssFeedFetcher
import com.blogzip.crawler.service.WebScrapper
import jakarta.annotation.PreDestroy
import org.springframework.batch.core.configuration.annotation.JobScope
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class CrawlerConfig(
) {

    // 주입받는 잡만 webdriver 초기화
    @JobScope
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

    @Bean
    fun htmlCompressor(): HtmlCompressor {
        return HtmlCompressor()
    }
}