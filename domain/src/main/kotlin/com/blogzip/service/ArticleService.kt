package com.blogzip.service

import com.blogzip.domain.Article
import com.blogzip.domain.ArticleRepository
import com.blogzip.domain.User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class ArticleService(private val repository: ArticleRepository) {

    @Transactional
    fun saveIfNotExists(articles: Iterable<Article>): List<Article> {
        return articles.filter { !repository.existsByUrl(it.url) }
            .map { repository.save(it) }
            .toList()
    }

    @Transactional(readOnly = true)
    fun findAllByUserAndCreatedDate(user: User, createdDate: LocalDate): List<Article> {
        return repository.findAllByUserAndCreatedDate(user, createdDate)
    }
}