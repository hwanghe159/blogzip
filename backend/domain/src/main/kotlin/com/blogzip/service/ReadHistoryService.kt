package com.blogzip.service

import com.blogzip.common.DomainException
import com.blogzip.common.ErrorCode
import com.blogzip.domain.ArticleRepository
import com.blogzip.domain.ReadHistory
import com.blogzip.domain.ReadHistoryRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class ReadHistoryService(
  private val readHistoryRepository: ReadHistoryRepository,
  private val articleRepository: ArticleRepository,
) {

  fun save(userId: Long, articleUrl: String) {
    val article = articleRepository.findByUrl(articleUrl)
      ?: throw DomainException(ErrorCode.ARTICLE_NOT_FOUND)
    readHistoryRepository.save(ReadHistory(userId = userId, articleId = article.id!!))
  }

  fun save(userId: Long, articleId: Long) {
    val article = articleRepository.findByIdOrNull(articleId)
      ?: throw DomainException(ErrorCode.ARTICLE_NOT_FOUND)
    readHistoryRepository.save(ReadHistory(userId = userId, articleId = article.id!!))
  }
}