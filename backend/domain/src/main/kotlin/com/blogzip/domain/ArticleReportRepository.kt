package com.blogzip.domain

import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface ArticleReportRepository : JpaRepository<ArticleReport, Long> {

  fun findByArticleIdAndUserId(articleId: Long, userId: Long): ArticleReport?

  fun findAllByArticleIdOrderByCreatedAtDesc(articleId: Long): List<ArticleReport>

  @Query(
    """
    select articleReport
    from ArticleReport articleReport
    where articleReport.status = :status
      and (:next is null or articleReport.id < :next)
    """
  )
  fun searchByStatus(
    status: ArticleReport.Status,
    next: Long?,
    pageable: Pageable,
  ): List<ArticleReport>

  @Query(
    """
    select articleReport.articleId as articleId,
           count(articleReport.id) as totalCount,
           sum(case when articleReport.status = :receivedStatus then 1 else 0 end) as receivedCount
    from ArticleReport articleReport
    where articleReport.articleId in :articleIds
    group by articleReport.articleId
    """
  )
  fun countByArticleIdIn(
    articleIds: Collection<Long>,
    receivedStatus: ArticleReport.Status,
  ): List<ArticleReportCountProjection>

  interface ArticleReportCountProjection {
    val articleId: Long
    val totalCount: Long
    val receivedCount: Long
  }
}
