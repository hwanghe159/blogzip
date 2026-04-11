package com.blogzip.api.dto.admin

import com.blogzip.domain.ArticleSummary
import java.time.LocalDateTime

data class ArticleSummaryResponse(
  val id: Long,
  val articleId: Long,
  val summary: String,
  val summarizedBy: String,
  val isApplied: Boolean,
  val createdAt: LocalDateTime,
  val updatedAt: LocalDateTime,
) {
  companion object {
    fun from(articleSummary: ArticleSummary): ArticleSummaryResponse {
      return ArticleSummaryResponse(
        id = articleSummary.id!!,
        articleId = articleSummary.articleId,
        summary = articleSummary.summary,
        summarizedBy = articleSummary.summarizedBy,
        isApplied = articleSummary.isApplied,
        createdAt = articleSummary.createdAt,
        updatedAt = articleSummary.updatedAt,
      )
    }
  }
}
