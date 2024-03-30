package com.blogzip.service

import com.blogzip.domain.Article
import com.blogzip.domain.ArticleRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ArticleService(private val repository: ArticleRepository) {

    @Transactional
    fun saveIfNotExists(articles: Iterable<Article>): List<Article> {
        return articles.filter { !repository.existsByUrl(it.url) }
            .map { repository.save(it) }
            .toList()
    }
}