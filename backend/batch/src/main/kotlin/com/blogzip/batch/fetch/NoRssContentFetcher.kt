package com.blogzip.batch.fetch

import com.blogzip.crawler.service.HtmlCompressor
import com.blogzip.crawler.service.WebScrapper
import com.blogzip.domain.Article
import com.blogzip.domain.Blog
import com.blogzip.logger
import com.blogzip.service.ArticleQueryService
import com.blogzip.slack.SlackSender
import com.blogzip.slack.SlackSender.SlackChannel.ERROR_LOG
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class NoRssContentFetcher(
  private val slackSender: SlackSender,
  private val htmlCompressor: HtmlCompressor,
  private val articleQueryService: ArticleQueryService,
  private val chromeWebScrapper: WebScrapper,
) : NewArticlesFetcher {

  val log = logger()

  override fun fetchArticles(blog: Blog, from: LocalDate): FetchArticlesResult {
    if (blog.urlCssSelector == null) {
      val errorMessage = "css selector가 없어 새 글 가져오기 실패. url=${blog.url}"
      log.error(errorMessage)
      slackSender.sendMessageAsync(channel = ERROR_LOG, errorMessage)
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
    val scrapResult =
      chromeWebScrapper.getArticles(blog.url, blog.urlCssSelector!!, articleUrls)
    if (scrapResult.isFailed()) {
      slackSender.sendMessageAsync(ERROR_LOG, "${blog.url} 크롤링 부분/전체 실패")
      slackSender.sendStackTraceAsync(ERROR_LOG, scrapResult.failCause!!)
    }
    val failures = mutableListOf<FetchFailure>()
    if (scrapResult.isFailed()) {
      failures += FetchFailure(
        blogId = blog.id,
        blogUrl = blog.url,
        rssStatus = blog.rssStatus,
        reason = "ARTICLE_LIST_CRAWL_FAILED",
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
}
