package com.blogzip.api.dto

import com.blogzip.dto.Blog
import java.time.LocalDate

data class ReadLaterDetailResponse private constructor(
  val id: Long,
  val article: ArticleWithKeywordsResponse,
) {
  companion object {
    fun of(
      readLater: com.blogzip.dto.ReadLater,
      keywords: List<String>
    ): ReadLaterDetailResponse {
      return ReadLaterDetailResponse(
        id = readLater.id,
        article = ArticleWithKeywordsResponse.from(readLater.article, keywords),
      )
    }
  }

  data class ArticleWithKeywordsResponse private constructor(
    val id: Long,
    val blog: BlogResponse,
    val title: String,
    val url: String,
    var summary: String,
    val keywords: List<String>,
    var createdDate: LocalDate,
  ) {
    companion object {
      fun from(
        article: com.blogzip.dto.Article,
        keywords: List<String>
      ): ArticleWithKeywordsResponse {
        return ArticleWithKeywordsResponse(
          id = article.id,
          blog = BlogResponse.from(article.blog),
          title = article.title,
          url = article.url,
          summary = article.summary,
          keywords = keywords,
          createdDate = article.createdDate,
        )
      }
    }

    data class BlogResponse private constructor(
      val id: Long,
      val name: String,
      val url: String,
      val image: String?,
    ) {
      companion object {
        fun from(blog: Blog): BlogResponse {
          return BlogResponse(
            id = blog.id,
            name = blog.name,
            url = blog.url,
            image = blog.image,
          )
        }
      }
    }
  }
}
