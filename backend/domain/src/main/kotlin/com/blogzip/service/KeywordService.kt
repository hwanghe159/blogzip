package com.blogzip.service

import com.blogzip.common.DomainException
import com.blogzip.common.ErrorCode
import com.blogzip.domain.*
import com.blogzip.dto.HeadKeyword
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class KeywordService(
    private val keywordRepository: KeywordRepository,
    private val articleKeywordRepository: ArticleKeywordRepository,
    private val articleRepository: ArticleRepository,
) {

    @Transactional
    fun addArticleKeywords(articleId: Long, inputValues: List<String>) {
        val article = articleRepository.findByIdOrNull(articleId)
            ?: throw DomainException(ErrorCode.ARTICLE_NOT_FOUND)
        val keywords = saveAllIfNotExist(inputValues)

        val keywordIds = articleKeywordRepository.findAllByArticleId(articleId)
            .map { it.headKeywordId }
            .toSet()
        val articleKeywords = keywords
            .filter { it.isHead() }
            .filterNot { keywordIds.contains(it.id) }
            .map { ArticleKeyword(articleId = article.id!!, headKeywordId = it.id!!) }
        articleKeywordRepository.saveAll(articleKeywords)
    }

    @Transactional(readOnly = true)
    fun getKeywordDetails(articleId: Long): List<HeadKeyword> {
        val headKeywordIds = articleKeywordRepository.findAllByArticleId(articleId)
            .map { it.headKeywordId }
        return keywordRepository.findAllById(headKeywordIds)
            .map { HeadKeyword.from(it) }
    }

    private fun saveAllIfNotExist(keywordValues: List<String>): List<Keyword> {
        val existing = keywordRepository.findAllByValueIn(keywordValues)
            .map { it.value to it }
            .toMap()
        return keywordValues
            .map {
                if (existing[it] == null) {
                    keywordRepository.save(Keyword(value = it, head = null))
                } else {
                    existing[it]!!
                }
            }
    }
}