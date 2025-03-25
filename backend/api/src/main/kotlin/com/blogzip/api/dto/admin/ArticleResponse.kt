package com.blogzip.api.dto.admin

import com.blogzip.domain.Article
import com.blogzip.dto.HeadKeyword
import java.time.LocalDate

data class ArticleResponse private constructor(
  val id: Long,
  val blogId: Long,
  val title: String,
  val content: String,
  val url: String,
  val summary: String?,
  val summarizedBy: String?,
  val createdDate: LocalDate?,
  val keywords: List<HeadKeywordResponse>,
) {
  companion object {
    fun from(article: Article, headKeywords: List<HeadKeyword>): ArticleResponse {
      val keywords = headKeywords
        .map {
          HeadKeywordResponse(
            id = it.id,
            value = it.value,
            followers = it.followers.map {
              FollowerKeywordResponse(
                id = it.id,
                value = it.value,
                createdAt = it.createdAt,
              )
            },
            createdAt = it.createdAt,
          )
        }

      return ArticleResponse(
        id = article.id!!,
        blogId = article.blogId,
        title = article.title,
        content = article.content,
        url = article.url,
        summary = article.summary,
        summarizedBy = article.summarizedBy,
        createdDate = article.createdDate,
        keywords = keywords
      )
    }
  }
}