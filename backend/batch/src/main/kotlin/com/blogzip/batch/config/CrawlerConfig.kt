package com.blogzip.batch.config

import com.blogzip.logger
import com.blogzip.crawler.service.ChromeWebScrapper
import com.blogzip.crawler.service.HtmlCompressor
import com.blogzip.crawler.service.RssFeedFetcher
import com.blogzip.crawler.service.WebScrapper
import jakarta.annotation.PreDestroy
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.Collections

@Configuration
class CrawlerConfig(
  private val seleniumProperties: SeleniumProperties
) {
  private val log = logger()
  private val createdWebScrappers = Collections.synchronizedSet(mutableSetOf<WebScrapper>())

  @StepScope
  @Bean
  fun chromeWebScrapper(): WebScrapper {
    val webScrapper = ChromeWebScrapper.create(
      com.blogzip.crawler.config.SeleniumProperties(seleniumProperties.chromeOptions)
    )
    createdWebScrappers.add(webScrapper)
    return webScrapper
  }

  @Bean
  fun rssFeedFetcher(): RssFeedFetcher {
    return RssFeedFetcher.create()
  }

  @Bean
  fun htmlCompressor(): HtmlCompressor {
    return HtmlCompressor()
  }

  @PreDestroy
  fun quitWebDriversOnShutdown() {
    createdWebScrappers.forEach { webScrapper ->
      runCatching { webScrapper.endUse() }
        .onFailure { e -> log.warn("Batch 종료 시 WebScrapper 종료 실패", e) }
    }
    createdWebScrappers.clear()
  }
}
