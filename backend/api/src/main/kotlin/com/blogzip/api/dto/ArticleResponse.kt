package com.blogzip.api.dto

import com.blogzip.domain.Article
import com.blogzip.dto.SearchedArticles
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDate

data class ArticleResponse private constructor(
    val id: Long,
    val blog: BlogResponse,
    val title: String,
    val url: String,
    var summary: String,
    @get:JsonProperty("isReadLater")
    val isReadLater: Boolean,
    var createdDate: LocalDate,
) {
    companion object {
        fun from(article: SearchedArticles.Article): ArticleResponse {
            return ArticleResponse(
                id = article.id,
                blog = BlogResponse.from(article.blog),
                title = article.title,
                url = article.url,
                summary = article.summary,
                isReadLater = article.isReadLater,
                createdDate = article.createdDate,
            )
        }
    }
}