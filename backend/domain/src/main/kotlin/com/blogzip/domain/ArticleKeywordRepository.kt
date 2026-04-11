package com.blogzip.domain

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface ArticleKeywordRepository : JpaRepository<ArticleKeyword, Long> {

  fun findAllByArticleId(articleId: Long): List<ArticleKeyword>

  fun findAllByArticleIdIn(articleIds: Collection<Long>): List<ArticleKeyword>

  fun findAllByHeadKeywordId(headKeywordId: Long): List<ArticleKeyword>

  @Query(
    """
    select articleKeyword.headKeywordId as headKeywordId, count(articleKeyword.id) as mappingCount
    from ArticleKeyword articleKeyword
    group by articleKeyword.headKeywordId
    """
  )
  fun countMappingsByHeadKeywordId(): List<HeadKeywordMappingCount>

  interface HeadKeywordMappingCount {
    val headKeywordId: Long
    val mappingCount: Long
  }
}
