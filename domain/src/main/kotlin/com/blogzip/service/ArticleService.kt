package com.blogzip.service

import com.blogzip.common.DomainException
import com.blogzip.common.ErrorCode
import com.blogzip.common.logger
import com.blogzip.domain.*
import com.blogzip.dto.SearchedArticles
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class ArticleService(
    private val articleRepository: ArticleRepository,
    private val readLaterRepository: ReadLaterRepository,
) {

    var log = logger()

    // 메인에 노출될 글 조회 (비로그인)
    @Transactional(readOnly = true)
    fun search(from: LocalDate, to: LocalDate?, next: Long?, size: Int): SearchedArticles {
        val articles = articleRepository.search(
            from,
            to ?: LocalDate.now(),
            next,
            PageRequest.of(
                0, size + 1,
                Sort.by("createdDate").descending()
                    .and(Sort.by("id").descending())
            )
        )
        val existsNext = articles.size == size + 1

        return SearchedArticles.of(
            articles = articles.take(size),
            next = if (existsNext) articles.take(size).last().id else null,
            emptySet(),
        )
    }

    // 메인에 노출될 글 조회 (로그인)
    @Transactional(readOnly = true)
    fun searchMy(
        blogs: Collection<Blog>,
        from: LocalDate,
        to: LocalDate?,
        next: Long?,
        size: Int,
        userId: Long,
    ): SearchedArticles {
        val articles = articleRepository.searchMy(
            blogs,
            from,
            to ?: LocalDate.now(),
            next,
            PageRequest.of(
                0, size + 1,
                Sort.by("createdDate").descending()
                    .and(Sort.by("id").descending())
            )
        )

        val readLaterArticleIds = readLaterRepository.findAllByUserId(userId, null)
            .map { it.article!!.id!! }
            .toSet()

        val existsNext = articles.size == size + 1

        return SearchedArticles.of(
            articles = articles.take(size),
            next = if (existsNext) articles.take(size).last().id else null,
            readLaterArticleIds,
        )
    }

    @Transactional(readOnly = true)
    fun existsByUrl(url: String): Boolean {
        return articleRepository.existsByUrl(url)
    }

    @Transactional(readOnly = true)
    fun findById(id: Long): Article {
        return articleRepository.findByIdOrNull(id)
            ?: throw DomainException(ErrorCode.ARTICLE_NOT_FOUND)
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun save(article: Article) {
        if (!articleRepository.existsByUrl(article.url)) {
            articleRepository.save(article)
        }
    }

    @Transactional(readOnly = true)
    fun findAllSummarizeTarget(startDate: LocalDate): List<Article> {
        return articleRepository.findAllByCreatedDateGreaterThanEqualAndSummaryIsNull(startDate)
    }

    @Transactional
    fun updateSummary(id: Long, summary: String, summarizedBy: String) {
        val article = articleRepository.findByIdOrNull(id)
            ?: throw DomainException(ErrorCode.ARTICLE_NOT_FOUND)
        article.summary = summary
        article.summarizedBy = summarizedBy
    }
}