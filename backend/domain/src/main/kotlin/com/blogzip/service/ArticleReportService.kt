package com.blogzip.service

import com.blogzip.common.DomainException
import com.blogzip.common.ErrorCode
import com.blogzip.domain.ArticleReport
import com.blogzip.domain.ArticleReportRepository
import com.blogzip.domain.ArticleRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

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
}
