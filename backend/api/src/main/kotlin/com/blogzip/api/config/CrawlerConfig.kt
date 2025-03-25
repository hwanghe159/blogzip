package com.blogzip.api.config

import com.blogzip.crawler.service.BlogMetadataScrapper
import com.blogzip.crawler.service.RssFeedFetcher
import jakarta.annotation.PreDestroy
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class CrawlerConfig(
  private val seleniumProperties: SeleniumProperties
) {

  private lateinit var blogMetadataScrapper: BlogMetadataScrapper

  @Bean
  fun blogMetadataScrapper(): BlogMetadataScrapper {
    blogMetadataScrapper = BlogMetadataScrapper.create(
      com.blogzip.crawler.config.SeleniumProperties(seleniumProperties.chromeOptions)
    )
    return blogMetadataScrapper
  }

  @PreDestroy
  fun quitWebDriver() {
    blogMetadataScrapper.endUse()
  }

  @Bean
  fun rssFeedFetcher(): RssFeedFetcher {
    return RssFeedFetcher.create()
  }
}