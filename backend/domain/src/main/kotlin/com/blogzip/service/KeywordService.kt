package com.blogzip.service

import com.blogzip.common.DomainException
import com.blogzip.common.ErrorCode
import com.blogzip.domain.*
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class KeywordService(
    private val keywordRepository: KeywordRepository,
    private val articleKeywordRepository: ArticleKeywordRepository,
    private val articleRepository: ArticleRepository,
) {

    fun addArticleKeywords(articleId: Long, inputValues: List<String>) {
        val article = articleRepository.findByIdOrNull(articleId)
            ?: throw DomainException(ErrorCode.ARTICLE_NOT_FOUND)

        val keywords = saveAllIfNotExist(inputValues)

        // todo uk 검사, lazy loading 실패
        val articleKeywords = keywords
            .filter { it.isHead() }
            .map { ArticleKeyword(articleId = article.id!!, headKeywordId = it.id!!) }
        articleKeywordRepository.saveAll(articleKeywords)
    }

    fun getKeywordDetails(articleId: Long): List<Keyword> {
        val headKeywordIds = articleKeywordRepository.findAllByArticleId(articleId)
            .map { it.headKeywordId }
        return keywordRepository.findAllById(headKeywordIds)
    }

    fun saveAllIfNotExist(keywordValues: List<String>): List<Keyword> {
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