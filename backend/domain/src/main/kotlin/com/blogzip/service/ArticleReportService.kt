package com.blogzip.service

import com.blogzip.common.DomainException
import com.blogzip.common.ErrorCode
import com.blogzip.domain.ArticleReport
import com.blogzip.domain.ArticleReportRepository
import com.blogzip.domain.ArticleRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

data class ArticleReportCount(
  val totalCount: Int,
  val receivedCount: Int,
)

data class PagedArticleReports(
  val items: List<ArticleReport>,
  val next: Long?,
)

@Service
class ArticleReportService(
  private val articleRepository: ArticleRepository,
  private val articleReportRepository: ArticleReportRepository,
) {

  @Transactional
  fun report(
    userId: Long,
    articleId: Long,
    reason: String,
    detail: String?,
  ): ArticleReport {
    if (!articleRepository.existsById(articleId)) {
      throw DomainException(ErrorCode.ARTICLE_NOT_FOUND)
    }
    val normalizedReason = reason.trim()
    val normalizedDetail = detail?.trim()?.takeIf { it.isNotBlank() }

    val existingReport = articleReportRepository.findByArticleIdAndUserId(articleId, userId)
    if (existingReport != null) {
      existingReport.update(normalizedReason, normalizedDetail)
      return existingReport
    }
    return articleReportRepository.save(
      ArticleReport(
        articleId = articleId,
        userId = userId,
        reason = normalizedReason,
        detail = normalizedDetail,
      )
    )
  }

  @Transactional(readOnly = true)
  fun getAllByArticleId(articleId: Long): List<ArticleReport> {
    if (!articleRepository.existsById(articleId)) {
      throw DomainException(ErrorCode.ARTICLE_NOT_FOUND)
    }
    return articleReportRepository.findAllByArticleIdOrderByCreatedAtDesc(articleId)
  }

  @Transactional(readOnly = true)
  fun getByStatus(status: ArticleReport.Status, next: Long?, size: Int): PagedArticleReports {
    val reports = articleReportRepository.searchByStatus(
      status = status,
      next = next,
      pageable = reportPageRequest(size),
    )
    val existsNext = reports.size == size + 1
    val finalReports = reports.take(size)
    return PagedArticleReports(
      items = finalReports,
      next = if (existsNext) finalReports.last().id else null,
    )
  }

  @Transactional(readOnly = true)
  fun getCountsByArticleIds(articleIds: Collection<Long>): Map<Long, ArticleReportCount> {
    if (articleIds.isEmpty()) {
      return emptyMap()
    }
    return articleReportRepository.countByArticleIdIn(
      articleIds = articleIds,
      receivedStatus = ArticleReport.Status.RECEIVED,
    ).associate { projection ->
      projection.articleId to ArticleReportCount(
        totalCount = projection.totalCount.toInt(),
        receivedCount = projection.receivedCount.toInt(),
      )
    }
  }

  @Transactional
  fun updateStatus(reportId: Long, status: ArticleReport.Status): ArticleReport {
    val report = articleReportRepository.findByIdOrNull(reportId)
      ?: throw DomainException(ErrorCode.ARTICLE_REPORT_NOT_FOUND)
    report.status = status
    return report
  }

  private fun reportPageRequest(size: Int): PageRequest {
    return PageRequest.of(
      0,
      size + 1,
      Sort.by("id").descending(),
    )
  }
}
