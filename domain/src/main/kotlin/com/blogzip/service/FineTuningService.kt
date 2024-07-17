package com.blogzip.service

import com.blogzip.domain.Article
import com.blogzip.domain.FineTuning
import com.blogzip.domain.FineTuningRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class FineTuningService(
    private val repository: FineTuningRepository,
) {

    @Transactional(readOnly = true)
    fun findAll(): List<FineTuning> {
        return repository.findAll()
    }

    @Transactional(readOnly = true)
    fun findByArticleId(articleId: Long): FineTuning? {
        return repository.findByArticleId(articleId)
    }

    @Transactional
    fun update(article: Article, tunedSummary: String): FineTuning {
        return repository.findByArticleId(article.id!!)
            ?.update(tunedSummary)
            ?: repository.save(FineTuning(article = article, summary = tunedSummary))
    }
}