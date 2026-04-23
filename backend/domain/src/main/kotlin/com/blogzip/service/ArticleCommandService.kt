package com.blogzip.service

import com.blogzip.common.DomainException
import com.blogzip.common.ErrorCode
import com.blogzip.domain.Article
import com.blogzip.domain.ArticleRepository
import com.blogzip.domain.ArticleSummary
import com.blogzip.domain.ArticleSummaryRepository
import com.blogzip.logger
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

data class ArticleCreatedDateUpdateCommand(
  val articleId: Long,
  val createdDate: LocalDate,
)

data class ArticleCreatedDateUpdateResult(
  val articleId: Long,
  val beforeCreatedDate: LocalDate?,
  val createdDate: LocalDate,
)

data class ArticleVisibilityUpdateResult(
  val articleId: Long,
  val beforeIsVisible: Boolean,
  val isVisible: Boolean,
)

@Service
class ArticleCommandService(
  private val articleRepository: ArticleRepository,
  private val articleSummaryRepository: ArticleSummaryRepository,
) {

  var log = logger()

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  fun save(article: Article) {
    if (!articleRepository.existsByUrl(article.url)) {
      articleRepository.save(article)
    }
  }

  @Transactional
  fun updateSummary(id: Long, summary: String, summarizedBy: String) {
    createAndApplySummary(
      articleId = id,
      summary = summary.trim(),
      summarizedBy = summarizedBy.trim(),
    )
  }

  @Transactional
  fun createSummaryCandidate(articleId: Long, summary: String, summarizedBy: String): ArticleSummary {
    val article = articleRepository.findByIdOrNull(articleId)
      ?: throw DomainException(ErrorCode.ARTICLE_NOT_FOUND)
    return articleSummaryRepository.save(
      ArticleSummary(
        articleId = article.id!!,
        summary = summary.trim(),
        summarizedBy = summarizedBy.trim(),
        isApplied = false,
      )
    )
  }

  @Transactional
  fun applySummary(summaryId: Long): ArticleSummary {
    val articleSummary = articleSummaryRepository.findByIdOrNull(summaryId)
      ?: throw DomainException(ErrorCode.ARTICLE_SUMMARY_NOT_FOUND)
    articleRepository.findByIdOrNull(articleSummary.articleId)
      ?: throw DomainException(ErrorCode.ARTICLE_NOT_FOUND)
    articleSummaryRepository.clearAppliedByArticleId(articleSummary.articleId)
    articleSummary.apply()
    return articleSummary
  }

  @Transactional(readOnly = true)
  fun getSummaries(articleId: Long): List<ArticleSummary> {
    val article = articleRepository.findByIdOrNull(articleId)
      ?: throw DomainException(ErrorCode.ARTICLE_NOT_FOUND)
    return articleSummaryRepository.findAllByArticleIdOrderByCreatedAtDesc(article.id!!)
  }

  @Transactional(readOnly = true)
  fun getAppliedSummary(articleId: Long): ArticleSummary? {
    articleRepository.findByIdOrNull(articleId)
      ?: throw DomainException(ErrorCode.ARTICLE_NOT_FOUND)
    return articleSummaryRepository.findByArticleIdAndIsAppliedTrue(articleId)
  }

  @Transactional(readOnly = true)
  fun getAppliedSummaries(articleIds: Collection<Long>): Map<Long, ArticleSummary> {
    if (articleIds.isEmpty()) {
      return emptyMap()
    }
    return articleSummaryRepository.findAllByArticleIdInAndIsAppliedTrue(articleIds)
      .associateBy { it.articleId }
  }

  @Transactional
  fun updateCreatedDate(articleId: Long, createdDate: LocalDate): ArticleCreatedDateUpdateResult {
    val article = articleRepository.findByIdOrNull(articleId)
      ?: throw DomainException(ErrorCode.ARTICLE_NOT_FOUND)
    val beforeCreatedDate = article.createdDate
    article.createdDate = createdDate
    return ArticleCreatedDateUpdateResult(
      articleId = article.id!!,
      beforeCreatedDate = beforeCreatedDate,
      createdDate = article.createdDate!!,
    )
  }

  @Transactional
  fun updateCreatedDates(commands: List<ArticleCreatedDateUpdateCommand>): List<ArticleCreatedDateUpdateResult> {
    if (commands.isEmpty()) {
      return emptyList()
    }
    val articleById = articleRepository.findAllById(commands.map { it.articleId })
      .associateBy { it.id!! }
    return commands.map { command ->
      val article = articleById[command.articleId]
        ?: throw DomainException(ErrorCode.ARTICLE_NOT_FOUND)
      val beforeCreatedDate = article.createdDate
      article.createdDate = command.createdDate
      ArticleCreatedDateUpdateResult(
        articleId = article.id!!,
        beforeCreatedDate = beforeCreatedDate,
        createdDate = article.createdDate!!,
      )
    }
  }

  @Transactional
  fun updateVisibility(articleId: Long, isVisible: Boolean): ArticleVisibilityUpdateResult {
    val article = articleRepository.findByIdOrNull(articleId)
      ?: throw DomainException(ErrorCode.ARTICLE_NOT_FOUND)
    val beforeIsVisible = article.isVisible
    article.isVisible = isVisible
    return ArticleVisibilityUpdateResult(
      articleId = article.id!!,
      beforeIsVisible = beforeIsVisible,
      isVisible = article.isVisible,
    )
  }

  private fun createAndApplySummary(
    articleId: Long,
    summary: String,
    summarizedBy: String,
  ): ArticleSummary {
    articleRepository.findByIdOrNull(articleId)
      ?: throw DomainException(ErrorCode.ARTICLE_NOT_FOUND)
    articleSummaryRepository.clearAppliedByArticleId(articleId)
    val appliedSummary = articleSummaryRepository.save(
      ArticleSummary(
        articleId = articleId,
        summary = summary,
        summarizedBy = summarizedBy,
        isApplied = true,
      )
    )
    return appliedSummary
  }
}
