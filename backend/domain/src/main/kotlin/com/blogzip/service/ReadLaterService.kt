package com.blogzip.service

import com.blogzip.common.DomainException
import com.blogzip.common.ErrorCode
import com.blogzip.domain.ArticleRepository
import com.blogzip.domain.ReadLater
import com.blogzip.domain.ReadLaterRepository
import com.blogzip.domain.UserRepository
import com.blogzip.dto.ReadLaterAndArticle
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ReadLaterService(
    private val readLaterRepository: ReadLaterRepository,
    private val userRepository: UserRepository,
    private val articleRepository: ArticleRepository,
) {

    @Transactional(readOnly = true)
    fun findAllByUserId(userId: Long): List<ReadLaterAndArticle> {
        val readLaters =
            readLaterRepository.findAllByUserId(userId, Sort.by(Sort.Direction.DESC, "id"))
        val articleIds = readLaters.map { it.articleId }.toSet()
        val articles = articleRepository.findAllById(articleIds).map { it.id to it }.toMap()
        return readLaters
            .filter { articles[it.articleId] != null }
            .map { ReadLaterAndArticle(it, articles[it.articleId]!!) }
    }

    @Transactional
    fun save(userId: Long, articleId: Long): ReadLaterAndArticle {
        userRepository.findByIdOrNull(userId)
            ?: throw DomainException(ErrorCode.USER_NOT_FOUND)
        val article = (articleRepository.findByIdOrNull(articleId)
            ?: throw DomainException(ErrorCode.ARTICLE_NOT_FOUND))

        val readLater = (readLaterRepository.findByUserIdAndArticleId(userId, articleId)
            ?: readLaterRepository.save(ReadLater(userId = userId, articleId = articleId)))
        return ReadLaterAndArticle(readLater, article)
    }

    @Transactional
    fun delete(userId: Long, articleId: Long) {
        readLaterRepository.deleteAllByUserIdAndArticleId(userId, articleId)
    }
}