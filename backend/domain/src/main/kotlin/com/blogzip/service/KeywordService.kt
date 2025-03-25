package com.blogzip.service

import com.blogzip.common.DomainException
import com.blogzip.common.ErrorCode
import com.blogzip.domain.*
import com.blogzip.dto.HeadKeyword
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

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

  @Transactional(readOnly = true)
  fun getAllByArticleIds(articleIds: Collection<Long>): Map<Long, List<HeadKeyword>> {
    val mapping: Map<Long, List<ArticleKeyword>> = articleKeywordRepository
      .findAllByArticleIdIn(articleIds)
      .groupBy { it.articleId }

    val keywordIds = mapping.values.flatten().map { it.headKeywordId }.toSet()
    val keywords: Map<Long, Keyword> = keywordRepository.findAllById(keywordIds)
      .map { it.id!! to it }
      .toMap()

    val result = mutableMapOf<Long, List<HeadKeyword>>()
    for (articleId in articleIds) {
      val headKeywords = mapping[articleId]
        ?.mapNotNull {
          keywords[it.headKeywordId]
            ?.let { keyword -> HeadKeyword.from(keyword) }
        }
        ?: emptyList()
      result[articleId] = headKeywords
    }
    return Collections.unmodifiableMap(result)
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

  @Transactional
  fun update(value: String, toBeValue: String?, visible: Boolean?) {
    val target = keywordRepository.findByValue(value)
      ?: throw DomainException(ErrorCode.KEYWORD_NOT_FOUND)
    if (toBeValue != null) {
      updateValue(target, toBeValue)
    }
    if (visible != null) {
      target.updateVisible(visible)
    }
  }

  @Transactional
  fun merge(srcValue: String, destValue: String) {
    val source = keywordRepository.findByValue(srcValue)
      ?: throw DomainException(ErrorCode.KEYWORD_NOT_FOUND)
    val destination = keywordRepository.findByValue(destValue)
      ?: throw DomainException(ErrorCode.KEYWORD_NOT_FOUND)
    source.mergeInto(destination)
    moveMapping(source.id!!, destination.id!!)
  }

  private fun updateValue(keyword: Keyword, value: String) {
    if (keywordRepository.existsByValue(value)) {
      throw DomainException(ErrorCode.KEYWORD_UPDATE_FAILED)
    }
    keyword.updateValue(value)
  }

  private fun moveMapping(srcKeywordId: Long, destKeywordId: Long) {
    val articleKeywords = articleKeywordRepository.findAllByHeadKeywordId(srcKeywordId)
    val alreadyMappedArticleIds = articleKeywordRepository.findAllByHeadKeywordId(destKeywordId)
      .map { it.articleId }
      .toSet()

    for (articleKeyword in articleKeywords) {
      if (alreadyMappedArticleIds.contains(articleKeyword.articleId)) {
        articleKeywordRepository.delete(articleKeyword)
      } else {
        articleKeyword.changeHeadKeywordId(destKeywordId)
      }
    }
  }
}