package com.blogzip.api.dto

import com.blogzip.domain.Article
import com.blogzip.domain.FineTuning

data class TunedArticleResponse private constructor(
    val articleId: Long,
    val title: String,
    val url: String,
    val summary: String?,
    val tunedSummary: String?,
    val keywords: List<String>,
) {
    companion object {
        fun of(fineTuning: FineTuning, article: Article): TunedArticleResponse {
            return TunedArticleResponse(
                articleId = article.id!!,
                title = article.title,
                url = article.url,
                summary = article.summary,
                tunedSummary = fineTuning.summary,
                keywords = fineTuning.keywords.split(",").map { it.trim() },
            )
        }

        fun from(article: Article): TunedArticleResponse {
            return TunedArticleResponse(
                articleId = article.id!!,
                title = article.title,
                url = article.url,
                summary = article.summary,
                tunedSummary = null,
                keywords = emptyList(),
            )
        }
    }
}
