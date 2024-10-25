package com.blogzip.service

import com.blogzip.common.DomainException
import com.blogzip.common.ErrorCode
import com.blogzip.common.logger
import com.blogzip.domain.Article
import com.blogzip.domain.ArticleRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Service
class ArticleCommandService(
    private val articleRepository: ArticleRepository,
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
        val article = articleRepository.findByIdOrNull(id)
            ?: throw DomainException(ErrorCode.ARTICLE_NOT_FOUND)
        article.summary = summary
        article.summarizedBy = summarizedBy
    }
}