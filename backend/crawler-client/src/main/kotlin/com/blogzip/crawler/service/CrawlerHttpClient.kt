package com.blogzip.crawler.service

import com.blogzip.crawler.dto.BlogMetadata
import com.blogzip.logger
import org.springframework.web.reactive.function.client.WebClient
import java.time.Duration

class CrawlerHttpClient(
  private val webClient: WebClient,
) {
  private val log = logger()

  fun getMetadata(url: String): BlogMetadata? {
    return runCatching {
      webClient.post()
        .uri("/metadata/fetch")
        .bodyValue(MetadataRequest(url))
        .retrieve()
        .bodyToMono(BlogMetadata::class.java)
        .block(Duration.ofSeconds(60))
    }.onFailure { e ->
      log.error("원격 crawler metadata 조회 실패. url=$url", e)
    }.getOrNull()
  }

  fun getContent(url: String): String? {
    return runCatching {
      webClient.post()
        .uri("/content/fetch")
        .bodyValue(ContentRequest(url))
        .retrieve()
        .bodyToMono(ContentResponse::class.java)
        .block(Duration.ofSeconds(60))
        ?.content
    }.onFailure { e ->
      log.error("원격 crawler content 조회 실패. url=$url", e)
    }.getOrNull()
  }

  fun getArticles(
    blogUrl: String,
    cssSelector: String,
    articleUrls: Set<String>,
  ): WebScrapper.ScrapResult {
    return runCatching {
      val response = webClient.post()
        .uri("/articles/fetch")
        .bodyValue(
          ArticlesRequest(
            blogUrl = blogUrl,
            cssSelector = cssSelector,
            articleUrls = articleUrls,
          )
        )
        .retrieve()
        .bodyToMono(ArticlesResponse::class.java)
        .block(Duration.ofSeconds(180))
        ?: throw IllegalStateException("원격 crawler 응답이 비어있습니다.")

      WebScrapper.ScrapResult(
        articles = response.articles.map {
          WebScrapper.Article(
            title = it.title,
            url = it.url,
            content = it.content,
          )
        },
        failCause = response.failCause?.message?.let { RuntimeException(it) },
      )
    }.getOrElse { throwable ->
      val exception = when (throwable) {
        is Exception -> throwable
        else -> RuntimeException(throwable.message, throwable)
      }
      log.error("원격 crawler article 조회 실패. blogUrl=$blogUrl, cssSelector=$cssSelector", exception)
      WebScrapper.ScrapResult(
        articles = emptyList(),
        failCause = exception,
      )
    }
  }

  private data class MetadataRequest(
    val url: String,
  )

  private data class ContentRequest(
    val url: String,
  )

  private data class ContentResponse(
    val content: String?,
  )

  private data class ArticlesRequest(
    val blogUrl: String,
    val cssSelector: String,
    val articleUrls: Set<String>,
  )

  private data class ArticlesResponse(
    val articles: List<RemoteArticle> = emptyList(),
    val failCause: RemoteFailCause? = null,
  )

  private data class RemoteArticle(
    val title: String,
    val url: String,
    val content: String,
  )

  private data class RemoteFailCause(
    val message: String?,
  )
}
