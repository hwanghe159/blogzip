package com.blogzip.api.dto

import com.blogzip.domain.Article
import com.blogzip.domain.FineTuning

data class TunedArticleResponse private constructor(
    val articleId: Long,
    val title: String,
    val url: String,
    val summary: String?,
    val tunedSummary: String?,
) {
    companion object {
        fun from(fineTuning: FineTuning): TunedArticleResponse {
            return TunedArticleResponse(
                articleId = fineTuning.article.id!!,
                title = fineTuning.article.title,
                url = fineTuning.article.url,
                summary = fineTuning.article.summary,
                tunedSummary = fineTuning.summary,
            )
        }

        fun from(article: Article): TunedArticleResponse {
            return TunedArticleResponse(
                articleId = article.id!!,
                title = article.title,
                url = article.url,
                summary = article.summary,
                tunedSummary = null,
            )
        }
    }
}
