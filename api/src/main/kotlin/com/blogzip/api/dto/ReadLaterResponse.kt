package com.blogzip.api.dto

import com.blogzip.domain.Article
import com.blogzip.domain.ReadLater
import java.time.LocalDate

data class ReadLaterResponse private constructor(
    val id: Long,
    val article: ArticleResponse,
) {
    companion object {
        fun of(readLater: ReadLater, article: Article): ReadLaterResponse {
            return ReadLaterResponse(
                id = readLater.id!!,
                article = ArticleResponse.from(article)
            )
        }
    }

    data class ArticleResponse private constructor(
        val id: Long,
        val blogId: Long,
        val title: String,
        val url: String,
        var summary: String,
        var createdDate: LocalDate,
    ) {
        companion object {
            fun from(article: Article): ArticleResponse {
                return ArticleResponse(
                    id = article.id!!,
                    blogId = article.blogId,
                    title = article.title,
                    url = article.url,
                    summary = article.summary!!,
                    createdDate = article.createdDate!!,
                )
            }
        }

    }
}
