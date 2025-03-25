package com.blogzip.dto

import java.time.LocalDate
import java.time.LocalDateTime

data class SearchedArticles private constructor(
  val articles: List<Article>,
  val next: Long?,
) {
  data class Article private constructor(
    val id: Long,
    val blog: Blog,
    val title: String,
    val content: String,
    val url: String,
    val summary: String,
    val summarizedBy: String,
    val isReadLater: Boolean,
    val createdDate: LocalDate,
  ) {
    data class Blog private constructor(
      val id: Long,
      val name: String,
      val url: String,
      val image: String?,
      val rssStatus: com.blogzip.domain.Blog.RssStatus,
      val rss: String?,
      val isShowOnMain: Boolean,
      val createdBy: Long,
      val createdAt: LocalDateTime,
    ) {
      companion object {
        fun from(blog: com.blogzip.domain.Blog): Blog {
          return Blog(
            id = blog.id!!,
            name = blog.name,
            url = blog.url,
            image = blog.image,
            rssStatus = blog.rssStatus,
            rss = blog.rss,
            isShowOnMain = blog.isShowOnMain,
            createdBy = blog.createdBy,
            createdAt = blog.createdAt,
          )
        }
      }
    }

    companion object {
      fun of(
        article: com.blogzip.domain.Article,
        blog: com.blogzip.domain.Blog,
        isReadLater: Boolean,
      ): Article {
        return Article(
          id = article.id!!,
          blog = Blog.from(blog),
          title = article.title,
          content = article.content,
          url = article.url,
          summary = article.summary!!,
          summarizedBy = article.summarizedBy!!,
          isReadLater = isReadLater,
          createdDate = article.createdDate!!,
        )
      }
    }
  }

  companion object {
    fun of(
      articleAndBlogs: List<ArticleAndBlog>,
      next: Long?,
      readLaterArticleIds: Set<Long>,
    ): SearchedArticles {
      return SearchedArticles(
        articles = articleAndBlogs.map {
          Article.of(
            article = it.article,
            blog = it.blog,
            readLaterArticleIds.contains(it.article.id)
          )
        }, next = next
      )
    }
  }
}