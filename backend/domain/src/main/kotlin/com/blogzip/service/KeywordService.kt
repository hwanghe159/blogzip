package com.blogzip.service

import com.blogzip.common.DomainException
import com.blogzip.common.ErrorCode
import com.blogzip.domain.*
import com.blogzip.dto.HeadKeyword
import com.blogzip.dto.KeywordOverview
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.Collections
import java.util.Locale

data class KeywordHeadUpdateResult(
  val keywordId: Long,
  val beforeHeadKeywordId: Long?,
  val afterHeadKeywordId: Long?,
)

@Service
class KeywordService(
  private val keywordRepository: KeywordRepository,
  private val articleKeywordRepository: ArticleKeywordRepository,
  private val articleRepository: ArticleRepository,
) {

  @Transactional
  fun createHeadKeyword(rawValue: String, isVisible: Boolean): Keyword {
    val value = rawValue.trim()
    if (value.isBlank()) {
      throw DomainException(ErrorCode.KEYWORD_UPDATE_FAILED)
    }

    val existing = keywordRepository.findByValue(value)
    if (existing != null) {
      existing.follow(null)
      existing.updateVisible(isVisible)
      return existing
    }

    return keywordRepository.save(
      Keyword(
        value = value,
        head = null,
        isVisible = isVisible,
      )
    )
  }

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

  @Transactional(readOnly = true)
  fun getOverview(): KeywordOverview {
    val keywords = keywordRepository.findAllWithHead()
    val mappingCountByHeadKeywordId = articleKeywordRepository.countMappingsByHeadKeywordId()
      .associate { it.headKeywordId to it.mappingCount.toInt() }

    val headKeywords = keywords
      .filter { it.isHead() }
      .sortedBy { it.value.lowercase(Locale.ROOT) }
    val followersByHeadKeywordId = keywords
      .filterNot { it.isHead() }
      .groupBy { it.head!!.id!! }

    return KeywordOverview(
      totalKeywordCount = keywords.size,
      headKeywordCount = headKeywords.size,
      followerKeywordCount = keywords.size - headKeywords.size,
      visibleKeywordCount = keywords.count { it.isVisible },
      hiddenKeywordCount = keywords.count { !it.isVisible },
      headKeywords = headKeywords.map { headKeyword ->
        KeywordOverview.HeadKeywordOverview(
          id = headKeyword.id!!,
          value = headKeyword.value,
          isVisible = headKeyword.isVisible,
          articleCount = mappingCountByHeadKeywordId[headKeyword.id] ?: 0,
          createdAt = headKeyword.createdAt,
          followers = followersByHeadKeywordId[headKeyword.id]
            .orEmpty()
            .sortedBy { it.value.lowercase(Locale.ROOT) }
            .map {
              KeywordOverview.FollowerKeywordOverview(
                id = it.id!!,
                value = it.value,
                isVisible = it.isVisible,
                createdAt = it.createdAt,
              )
            }
        )
      }
    )
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
  fun updateById(keywordId: Long, toBeValue: String?, visible: Boolean?) {
    val target = keywordRepository.findByIdOrNull(keywordId)
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
    merge(source, destination)
  }

  @Transactional
  fun mergeById(srcKeywordId: Long, destKeywordId: Long) {
    val source = keywordRepository.findByIdOrNull(srcKeywordId)
      ?: throw DomainException(ErrorCode.KEYWORD_NOT_FOUND)
    val destination = keywordRepository.findByIdOrNull(destKeywordId)
      ?: throw DomainException(ErrorCode.KEYWORD_NOT_FOUND)
    merge(source, destination)
  }

  @Transactional
  fun updateHead(keywordId: Long, headKeywordId: Long?): KeywordHeadUpdateResult {
    val keyword = keywordRepository.findByIdOrNull(keywordId)
      ?: throw DomainException(ErrorCode.KEYWORD_NOT_FOUND)
    val beforeHeadKeywordId = keyword.head?.id

    if (keyword.isHead()) {
      if (headKeywordId == null) {
        return KeywordHeadUpdateResult(
          keywordId = keyword.id!!,
          beforeHeadKeywordId = beforeHeadKeywordId,
          afterHeadKeywordId = null,
        )
      }
      val destination = keywordRepository.findByIdOrNull(headKeywordId)
        ?: throw DomainException(ErrorCode.KEYWORD_NOT_FOUND)
      if (!destination.isHead()) {
        throw DomainException(ErrorCode.KEYWORD_UPDATE_FAILED)
      }
      merge(keyword, destination)
      return KeywordHeadUpdateResult(
        keywordId = keyword.id!!,
        beforeHeadKeywordId = beforeHeadKeywordId,
        afterHeadKeywordId = destination.id,
      )
    }

    if (headKeywordId == null) {
      keyword.follow(null)
      return KeywordHeadUpdateResult(
        keywordId = keyword.id!!,
        beforeHeadKeywordId = beforeHeadKeywordId,
        afterHeadKeywordId = null,
      )
    }

    val destination = keywordRepository.findByIdOrNull(headKeywordId)
      ?: throw DomainException(ErrorCode.KEYWORD_NOT_FOUND)
    if (!destination.isHead()) {
      throw DomainException(ErrorCode.KEYWORD_UPDATE_FAILED)
    }
    keyword.follow(destination)
    return KeywordHeadUpdateResult(
      keywordId = keyword.id!!,
      beforeHeadKeywordId = beforeHeadKeywordId,
      afterHeadKeywordId = destination.id,
    )
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

  private fun merge(source: Keyword, destination: Keyword) {
    if (source.id == destination.id) {
      throw DomainException(ErrorCode.KEYWORD_UPDATE_FAILED)
    }
    source.mergeInto(destination)
    moveMapping(source.id!!, destination.id!!)
  }
}
