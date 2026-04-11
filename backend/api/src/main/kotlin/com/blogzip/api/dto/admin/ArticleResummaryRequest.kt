package com.blogzip.api.dto.admin

import com.blogzip.ai.summary.SummarizedArticleResult
import com.blogzip.domain.Article
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.Valid

data class ArticleResummaryPreviewRequest @JsonCreator(mode = JsonCreator.Mode.PROPERTIES) constructor(
  @JsonProperty("articleIds")
  val articleIds: List<Long> = emptyList(),
)

data class ArticleResummaryPreviewResponse(
  val items: List<ArticleResummaryPreviewItemResponse>,
)

data class ArticleResummaryPreviewItemResponse(
  val articleId: Long,
  val title: String?,
  val candidateSummaryId: Long?,
  val currentSummary: String?,
  val currentSummarizedBy: String?,
  val candidateSummary: String?,
  val candidateSummarizedBy: String?,
  val candidateKeywords: List<String>,
  val success: Boolean,
  val errorMessage: String?,
) {
  companion object {
    fun fromSuccess(
      article: Article,
      currentSummary: String?,
      currentSummarizedBy: String?,
      result: SummarizedArticleResult.Success,
      candidateSummaryId: Long,
    ): ArticleResummaryPreviewItemResponse {
      return ArticleResummaryPreviewItemResponse(
        articleId = article.id!!,
        title = article.title,
        candidateSummaryId = candidateSummaryId,
        currentSummary = currentSummary,
        currentSummarizedBy = currentSummarizedBy,
        candidateSummary = result.article.summary,
        candidateSummarizedBy = result.article.summarizedBy,
        candidateKeywords = result.article.keywords,
        success = true,
        errorMessage = null,
      )
    }

    fun fromFailure(
      articleId: Long,
      article: Article?,
      currentSummary: String?,
      currentSummarizedBy: String?,
      errorMessage: String,
    ): ArticleResummaryPreviewItemResponse {
      return ArticleResummaryPreviewItemResponse(
        articleId = articleId,
        title = article?.title,
        candidateSummaryId = null,
        currentSummary = currentSummary,
        currentSummarizedBy = currentSummarizedBy,
        candidateSummary = null,
        candidateSummarizedBy = null,
        candidateKeywords = emptyList(),
        success = false,
        errorMessage = errorMessage,
      )
    }
  }
}

data class ArticleResummaryApplyRequest @JsonCreator(mode = JsonCreator.Mode.PROPERTIES) constructor(
  @JsonProperty("items")
  val items: List<@Valid ArticleResummaryApplyItem> = emptyList(),
)

data class ArticleResummaryApplyItem @JsonCreator(mode = JsonCreator.Mode.PROPERTIES) constructor(
  @JsonProperty("articleSummaryId")
  val articleSummaryId: Long = 0,
  @JsonProperty("keywords")
  val keywords: List<String> = emptyList(),
  @JsonProperty("applyKeywords")
  val applyKeywords: Boolean = true,
)

data class ArticleResummaryApplyResponse(
  val items: List<ArticleResummaryApplyItemResponse>,
)

data class ArticleResummaryApplyItemResponse(
  val articleSummaryId: Long,
  val articleId: Long?,
  val applied: Boolean,
  val summaryApplied: Boolean,
  val keywordsApplied: Boolean,
  val errorMessage: String?,
)
