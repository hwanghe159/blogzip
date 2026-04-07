package com.blogzip.batch.config

import com.blogzip.crawler.service.CrawlerHttpClient
import com.blogzip.crawler.service.HtmlCompressor
import com.blogzip.crawler.service.RemoteWebScrapper
import com.blogzip.crawler.service.RssFeedFetcher
import com.blogzip.crawler.service.WebScrapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class CrawlerConfig(
  @Value("\${crawler.base-url}") private val crawlerBaseUrl: String,
) {

  @Bean
  fun chromeWebScrapper(): WebScrapper {
    return RemoteWebScrapper(crawlerHttpClient())
  }

  @Bean
  fun crawlerHttpClient(): CrawlerHttpClient {
    val webClient = WebClient.builder()
      .baseUrl(crawlerBaseUrl)
      .exchangeStrategies(
        ExchangeStrategies.builder()
          .codecs { it.defaultCodecs().maxInMemorySize(-1) }
          .build()
      )
      .build()
    return CrawlerHttpClient(webClient)
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
