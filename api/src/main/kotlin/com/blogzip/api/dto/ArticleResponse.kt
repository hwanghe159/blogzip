package com.blogzip.api.dto

import com.blogzip.domain.Article
import java.time.LocalDate

data class ArticleResponse private constructor(
    val id: Long,
    val blog: BlogResponse,
    val title: String,
    val url: String,
    var summary: String,
    var createdDate: LocalDate,
) {
    companion object {
        fun from(article: Article): ArticleResponse {
            return ArticleResponse(
                id = article.id!!,
                blog = BlogResponse.from(article.blog),
                title = article.title,
                url = article.url,
                summary = article.summary!!,
                createdDate = article.createdDate!!,
            )
        }
    }
}