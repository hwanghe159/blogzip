package com.blogzip.api.dto.admin

import com.blogzip.domain.Article
import com.blogzip.domain.ArticleReport
import com.blogzip.domain.ArticleSummary
import com.blogzip.domain.Blog
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.blogzip.service.ArticleReportCount
import java.time.LocalDate
import java.time.LocalDateTime

data class AdminRecentArticleResponse(
  val id: Long,
  val blogId: Long,
  val blogName: String,
  val blogUrl: String,
  val blogImage: String?,
  val title: String,
  val url: String,
  val summary: String?,
  val summarizedBy: String?,
  val createdDate: LocalDate,
  val reportCount: Int,
  val receivedReportCount: Int,
) {
  companion object {
    fun from(
      article: Article,
      blog: Blog,
      appliedSummary: ArticleSummary?,
      reportCount: ArticleReportCount?,
    ): AdminRecentArticleResponse {
      return AdminRecentArticleResponse(
        id = article.id!!,
        blogId = blog.id!!,
        blogName = blog.name,
        blogUrl = blog.url,
        blogImage = blog.image,
        title = article.title,
        url = article.url,
        summary = appliedSummary?.summary,
        summarizedBy = appliedSummary?.summarizedBy,
        createdDate = article.createdDate!!,
        reportCount = reportCount?.totalCount ?: 0,
        receivedReportCount = reportCount?.receivedCount ?: 0,
      )
    }
  }
}

data class AdminArticleReportResponse(
  val id: Long,
  val articleId: Long,
  val articleTitle: String,
  val articleUrl: String,
  val blogId: Long,
  val blogName: String,
  val userId: Long,
  val reason: String,
  val detail: String?,
  val status: ArticleReport.Status,
  val createdAt: LocalDateTime,
  val updatedAt: LocalDateTime,
) {
  companion object {
    fun from(
      report: ArticleReport,
      article: Article,
      blog: Blog,
    ): AdminArticleReportResponse {
      return AdminArticleReportResponse(
        id = report.id!!,
        articleId = report.articleId,
        articleTitle = article.title,
        articleUrl = article.url,
        blogId = blog.id!!,
        blogName = blog.name,
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

data class AdminArticleReportStatusUpdateRequest @JsonCreator(mode = JsonCreator.Mode.PROPERTIES) constructor(
  @JsonProperty("status")
  val status: ArticleReport.Status = ArticleReport.Status.RECEIVED,
)
