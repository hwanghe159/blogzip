package com.blogzip.batch.fetch

import com.blogzip.crawler.service.HtmlCompressor
import com.blogzip.crawler.service.WebScrapper
import com.blogzip.domain.Article
import com.blogzip.domain.Blog
import com.blogzip.logger
import com.blogzip.service.ArticleQueryService
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class NoRssContentFetcher(
  private val htmlCompressor: HtmlCompressor,
  private val articleQueryService: ArticleQueryService,
  private val chromeWebScrapper: WebScrapper,
) : NewArticlesFetcher {

  val log = logger()

  override fun fetchArticles(blog: Blog, from: LocalDate): FetchArticlesResult {
    if (blog.urlCssSelector == null) {
      val errorMessage = "css selector가 없어 새 글 가져오기 실패. url=${blog.url}"
      log.error(errorMessage)
      return FetchArticlesResult(
        articles = emptyList(),
        failures = listOf(
          FetchFailure(
            blogId = blog.id,
            blogUrl = blog.url,
            rssStatus = blog.rssStatus,
            reason = "CSS_SELECTOR_MISSING",
            detail = errorMessage,
          )
        )
      )
    }
    val articleUrls = articleQueryService.findAllByBlogId(blog.id!!)
      .map { it.url }
      .toSet()
    val urlCssSelector = blog.urlCssSelector!!
    val scrapResult =
      chromeWebScrapper.getArticles(blog.url, urlCssSelector, articleUrls)
    val cssSelectorInvalidOrChanged = isCssSelectorInvalidOrChanged(scrapResult, urlCssSelector)
    if (scrapResult.isFailed()) {
      log.error("${blog.url} 크롤링 부분/전체 실패", scrapResult.failCause)
    }
    val failures = mutableListOf<FetchFailure>()
    if (scrapResult.isFailed()) {
      failures += FetchFailure(
        blogId = blog.id,
        blogUrl = blog.url,
        rssStatus = blog.rssStatus,
        reason = if (cssSelectorInvalidOrChanged)
          "CSS_SELECTOR_INVALID_OR_CHANGED"
        else
          "ARTICLE_LIST_CRAWL_FAILED",
        detail = scrapResult.failCause?.message,
      )
    }

    val newArticles = scrapResult.articles
      .distinctBy { it.url }
      .filterNot { it.url.startsWith("chrome-extension://") }
      .filterNot { articleUrls.contains(it.url) }
      .map {
        Article(
          blogId = blog.id!!,
          title = it.title,
          content = htmlCompressor.compress(it.content),
          url = it.url,
          // 신규 등록 블로그의 글은 createdDate=1970/01/01로 고정
          createdDate = if (blog.isNew()) LocalDate.EPOCH else from,
        )
      }
    return FetchArticlesResult(
      articles = newArticles,
      failures = failures,
    )
  }

  private fun isCssSelectorInvalidOrChanged(
    scrapResult: WebScrapper.ScrapResult,
    cssSelector: String
  ): Boolean {
    val message = scrapResult.failCause?.message?.lowercase() ?: return false
    val selectorText = cssSelector.lowercase()

    if (!message.contains("selector")) {
      return false
    }

    return message.contains("waiting for selector")
      || message.contains("valid selector")
      || message.contains(selectorText)
  }
}
