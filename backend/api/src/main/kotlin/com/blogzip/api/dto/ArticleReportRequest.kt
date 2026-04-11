package com.blogzip.api.dto

import com.blogzip.domain.ArticleReport
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

data class ArticleReportRequest(
  @field:NotBlank(message = "reason은 필수입니다.")
  @field:Size(max = 100, message = "reason은 100자 이하여야 합니다.")
  val reason: String,

  @field:Size(max = 1000, message = "detail은 1000자 이하여야 합니다.")
  val detail: String? = null,
)

data class ArticleReportResponse(
  val id: Long,
  val articleId: Long,
  val userId: Long,
  val reason: String,
  val detail: String?,
  val status: ArticleReport.Status,
  val createdAt: LocalDateTime,
  val updatedAt: LocalDateTime,
) {
  companion object {
    fun from(report: ArticleReport): ArticleReportResponse {
      return ArticleReportResponse(
        id = report.id!!,
        articleId = report.articleId,
        userId = report.userId,
        reason = report.reason,
        detail = report.detail,
        status = report.status,
        createdAt = report.createdAt,
        updatedAt = report.updatedAt,
      )
    }
  }
}
