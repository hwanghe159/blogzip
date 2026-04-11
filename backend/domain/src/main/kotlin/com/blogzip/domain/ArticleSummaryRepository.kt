package com.blogzip.domain

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface ArticleSummaryRepository : JpaRepository<ArticleSummary, Long> {

  fun findAllByArticleIdOrderByCreatedAtDesc(articleId: Long): List<ArticleSummary>
  fun findByArticleIdAndIsAppliedTrue(articleId: Long): ArticleSummary?
  fun findAllByArticleIdInAndIsAppliedTrue(articleIds: Collection<Long>): List<ArticleSummary>

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query(
    """
    update ArticleSummary articleSummary
    set articleSummary.isApplied = false
    where articleSummary.articleId = :articleId
      and articleSummary.isApplied = true
    """
  )
  fun clearAppliedByArticleId(articleId: Long): Int
}
