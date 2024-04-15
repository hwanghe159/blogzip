package com.blogzip.service

import com.blogzip.common.logger
import com.blogzip.domain.Article
import com.blogzip.domain.ArticleRepository
import com.blogzip.domain.User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class ArticleService(private val repository: ArticleRepository) {

    var log = logger()

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun saveAll(articles: Iterable<Article>): List<Article> {
        return articles.mapNotNull { article ->
            try {
                repository.save(article)
            } catch (e: Exception) {
                log.error("article 저장 실패. $article")
                null
            }
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun save(article: Article): Article {
        return repository.save(article)
    }

    @Transactional(readOnly = true)
    fun findAllByUserAndCreatedDate(user: User, createdDate: LocalDate): List<Article> {
        return repository.findAllByUserAndCreatedDate(user, createdDate)
    }

    @Transactional(readOnly = true)
    fun findAllByCreatedDate(createdDate: LocalDate): List<Article> {
        return repository.findAllByCreatedDate(createdDate)
    }

    @Transactional
    fun findAllSummarizeTarget(createdDate: LocalDate): List<Article> {
        return repository.findAllByCreatedDateAndSummaryIsNull(createdDate)
    }
}