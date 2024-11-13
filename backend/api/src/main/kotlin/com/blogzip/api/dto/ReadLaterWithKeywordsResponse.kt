package com.blogzip.api.dto

import com.blogzip.domain.Article
import com.blogzip.domain.ReadLater
import java.time.LocalDate

data class ReadLaterWithKeywordsResponse private constructor(
    val id: Long,
    val article: ArticleWithKeywordsResponse,
) {
    companion object {
        fun of(
            readLater: ReadLater,
            article: Article,
            keywords: List<String>
        ): ReadLaterWithKeywordsResponse {
            return ReadLaterWithKeywordsResponse(
                id = readLater.id!!,
                article = ArticleWithKeywordsResponse.from(article, keywords),
            )
        }
    }

    data class ArticleWithKeywordsResponse private constructor(
        val id: Long,
        val blogId: Long,
        val title: String,
        val url: String,
        var summary: String,
        val keywords: List<String>,
        var createdDate: LocalDate,
    ) {
        companion object {
            fun from(article: Article, keywords: List<String>): ArticleWithKeywordsResponse {
                return ArticleWithKeywordsResponse(
                    id = article.id!!,
                    blogId = article.blogId,
                    title = article.title,
                    url = article.url,
                    summary = article.summary!!,
                    keywords = keywords,
                    createdDate = article.createdDate!!,
                )
            }
        }

    }
}
