package com.blogzip.service

import com.blogzip.common.logger
import com.blogzip.domain.Article
import com.blogzip.domain.ArticleRepository
import com.blogzip.domain.User
import com.blogzip.dto.SearchedArticles
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class ArticleService(private val repository: ArticleRepository) {

    var log = logger()

    // 메인에 노출될 글 조회
    @Transactional(readOnly = true)
    fun search(from: LocalDate, to: LocalDate?, next: Long?, size: Int): SearchedArticles {
        val articles = repository.search(
            from,
            to ?: LocalDate.now(),
            next,
            PageRequest.of(0, size + 1, Sort.by(Sort.Direction.DESC, "id"))
        )
        val existsNext = articles.size == size + 1

        return SearchedArticles(
            articles = articles.take(size),
            next = if (existsNext) articles.take(size).last().id else null
        )
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun saveIfNotExists(articles: Iterable<Article>): List<Article> {
        return articles.filter { !repository.existsByUrl(it.url) }
            .map { repository.save(it) }
            .toList()
    }

    @Transactional(readOnly = true)
    fun existsByUrl(url: String): Boolean {
        return repository.existsByUrl(url)
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun save(article: Article) {
        if (!repository.existsByUrl(article.url)) {
            repository.save(article)
        }
    }

    @Transactional(readOnly = true)
    fun findAllByUserAndCreatedDate(user: User, createdDate: LocalDate): List<Article> {
        return repository.findAllByUserAndCreatedDate(user, createdDate)
    }

    @Transactional(readOnly = true)
    fun findAllByCreatedDate(createdDate: LocalDate): List<Article> {
        return repository.findAllByCreatedDate(createdDate)
    }

    fun findAllByCreatedDates(createdDates: List<LocalDate>): List<Article> {
        return repository.findAllByCreatedDateIn(createdDates)
    }

    @Transactional
    fun findAllSummarizeTarget(createdDate: LocalDate): List<Article> {
        return repository.findAllByCreatedDateAndSummaryIsNull(createdDate)
    }
}