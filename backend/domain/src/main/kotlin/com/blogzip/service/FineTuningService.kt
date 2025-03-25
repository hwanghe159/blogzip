package com.blogzip.service

import com.blogzip.common.DomainException
import com.blogzip.common.ErrorCode
import com.blogzip.domain.ArticleRepository
import com.blogzip.domain.FineTuning
import com.blogzip.domain.FineTuningRepository
import com.blogzip.dto.FineTuningAndArticle
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class FineTuningService(
  private val fineTuningRepository: FineTuningRepository,
  private val articleRepository: ArticleRepository
) {

  @Transactional(readOnly = true)
  fun findAll(): List<FineTuningAndArticle> {
    val fineTunings = fineTuningRepository.findAll()
    val articleIds = fineTunings.map { it.articleId }.toSet()
    val articles = articleRepository.findAllById(articleIds)
      .map { it.id to it }.toMap()
    return fineTunings
      .filter { articles[it.articleId] != null }
      .map { FineTuningAndArticle(it, article = articles[it.articleId]!!) }
  }

  @Transactional(readOnly = true)
  fun findByArticleId(articleId: Long): FineTuning? {
    return fineTuningRepository.findByArticleId(articleId)
  }

  @Transactional
  fun update(
    articleId: Long,
    tunedSummary: String,
    keywords: List<String>
  ): FineTuningAndArticle {
    val article = articleRepository.findByIdOrNull(articleId)
      ?: throw DomainException(ErrorCode.ARTICLE_NOT_FOUND)
    val fineTuning = (fineTuningRepository.findByArticleId(article.id!!)
      ?.update(tunedSummary)
      ?: fineTuningRepository.save(
        FineTuning(
          articleId = articleId,
          summary = tunedSummary,
          keywords = keywords.joinToString(",") { it.trim() }
        )
      ))
    return FineTuningAndArticle(fineTuning, article)
  }
}