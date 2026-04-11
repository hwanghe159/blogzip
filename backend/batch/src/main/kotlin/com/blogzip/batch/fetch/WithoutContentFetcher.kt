package com.blogzip.batch.fetch

import com.blogzip.crawler.service.HtmlCompressor
import com.blogzip.crawler.service.RssFeedFetcher
import com.blogzip.crawler.service.WebScrapper
import com.blogzip.domain.Article
import com.blogzip.domain.Blog
import com.blogzip.logger
import com.blogzip.service.ArticleQueryService
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class WithoutContentFetcher(
  private val rssFeedFetcher: RssFeedFetcher,
  private val htmlCompressor: HtmlCompressor,
  private val articleQueryService: ArticleQueryService,
  private val chromeWebScrapper: WebScrapper,
) : NewArticlesFetcher {

  val log = logger()

  override fun fetchArticles(blog: Blog, from: LocalDate): FetchArticlesResult {
    if (blog.rss == null) {
      val errorMessage = "blog.rss가 없어 새 글 가져오기 실패. blog.id=${blog.id}"
      log.error(errorMessage)
      return FetchArticlesResult(
        articles = emptyList(),
        failures = listOf(
          FetchFailure(
            blogId = blog.id,
            blogUrl = blog.url,
            rssStatus = blog.rssStatus,
            reason = "RSS_URL_MISSING",
            detail = errorMessage,
          )
        )
      )
    }

    val articles = try {
      rssFeedFetcher.getArticles(blog.rss!!)
    } catch (e: Exception) {
      log.error("${blog.rss}의 글 가져오기 실패.", e)
      return FetchArticlesResult(
        articles = emptyList(),
        failures = listOf(
          FetchFailure(
            blogId = blog.id,
            blogUrl = blog.url,
            rssStatus = blog.rssStatus,
            reason = "RSS_FETCH_FAILED",
            detail = e.message,
          )
        )
      )
    }

    val candidateArticles = articles.filterNot { articleQueryService.existsByUrl(it.url) }
      .filter {
        if (it.createdDate == null) {
          true
        } else {
          from <= it.createdDate
        }
      }
    val contentFailedUrls = mutableListOf<String>()
    val newArticles = candidateArticles.mapNotNull {
        val content = chromeWebScrapper.getContent(it.url)
        if (content.isNullOrBlank()) {
          contentFailedUrls.add(it.url)
          null
        } else
          Article(
            blogId = blog.id!!,
            title = it.title,
            content = htmlCompressor.compress(content),
            url = it.url,
            createdDate =
            if (it.createdDate == null) {
              if (blog.isNew()) {
                LocalDate.EPOCH
              } else {
                LocalDate.now()
              }
            } else {
              it.createdDate
            }
          )
      }
    val failures = mutableListOf<FetchFailure>()
    if (contentFailedUrls.isNotEmpty()) {
      failures += FetchFailure(
        blogId = blog.id,
        blogUrl = blog.url,
        rssStatus = blog.rssStatus,
        reason = if (candidateArticles.isNotEmpty() && newArticles.isEmpty())
          "ARTICLE_CONTENT_CRAWL_FAILED_ALL"
        else
          "ARTICLE_CONTENT_CRAWL_FAILED_PARTIAL",
        detail = "failedCount=${contentFailedUrls.size}, sampleUrls=${contentFailedUrls.take(3)}${if (contentFailedUrls.size > 3) "..." else ""}",
      )
    }

    return FetchArticlesResult(
      articles = newArticles,
      failures = failures,
    )
  }
}
