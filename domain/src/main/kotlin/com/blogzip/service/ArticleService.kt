package com.blogzip.service

import com.blogzip.domain.Article
import com.blogzip.domain.ArticleRepository
import com.blogzip.domain.User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class ArticleService(private val repository: ArticleRepository) {

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun saveIfNotExists(articles: Iterable<Article>): List<Article> {
        return articles.filter { !repository.existsByUrl(it.url) }
            .map { repository.save(it) }
            .toList()
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun save(article: Article): Article {
        return repository.save(article)
    }

    @Transactional(readOnly = true)
    fun findAllByUserAndCreatedDate(user: User, createdDate: LocalDate): List<Article> {
        return repository.findAllByUserAndCreatedDate(user, createdDate)
    }

    @Transactional
    fun findAllSummarizeTarget(createdDate: LocalDate): List<Article> {
        return repository.findAllByCreatedDateAndSummaryIsNull(createdDate)
    }
}