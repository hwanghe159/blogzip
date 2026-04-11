package com.blogzip.batch.fetch

import com.blogzip.crawler.service.HtmlCompressor
import com.blogzip.crawler.service.RssFeedFetcher
import com.blogzip.domain.Article
import com.blogzip.domain.Blog
import com.blogzip.logger
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class WithContentFetcher(
  private val rssFeedFetcher: RssFeedFetcher,
  private val htmlCompressor: HtmlCompressor,
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
      val exception = RuntimeException("${blog.rss}의 글 가져오기 실패.", e)
      log.error(exception.message, exception)
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

    val contentMissingUrls = mutableListOf<String>()
    val newArticles = articles
      .filter {
        if (it.createdDate == null) {
          true
        } else {
          from <= it.createdDate
        }
      }
      .filter { article ->
        if (article.content == null) {
          contentMissingUrls.add(article.url)
          false
        } else {
          true
        }
      }
      .map {
        Article(
          blogId = blog.id!!,
          title = it.title,
          content = htmlCompressor.compress(it.content!!),
          url = it.url,

          // RSS 안에 글 작성 날짜가 주어지지 않는 경우도 있음
          createdDate =
          if (it.createdDate == null) {
            if (blog.isNew()) {
              LocalDate.EPOCH
            } else {
              LocalDate.now().minusDays(1)
            }
          } else {
            it.createdDate
          }
        )
      }
    val failures = mutableListOf<FetchFailure>()
    if (contentMissingUrls.isNotEmpty()) {
      failures += FetchFailure(
        blogId = blog.id,
        blogUrl = blog.url,
        rssStatus = blog.rssStatus,
        reason = "ARTICLE_CONTENT_MISSING_IN_RSS",
        detail = "missingCount=${contentMissingUrls.size}, sampleUrls=${contentMissingUrls.take(3)}${if (contentMissingUrls.size > 3) "..." else ""}",
      )
    }

    return FetchArticlesResult(
      articles = newArticles,
      failures = failures,
    )
  }
}
