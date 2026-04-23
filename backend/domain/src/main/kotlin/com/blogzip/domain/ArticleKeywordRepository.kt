package com.blogzip.domain

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDate

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

  @Query(
    """
    select keyword.value as keywordValue, count(articleKeyword.id) as mappingCount
    from ArticleKeyword articleKeyword, Article article, Keyword keyword
    where articleKeyword.articleId = article.id
      and articleKeyword.headKeywordId = keyword.id
      and keyword.head is null
      and keyword.isVisible = true
      and article.isVisible = true
      and article.blogId in :blogIds
      and article.createdDate >= :from
      and article.createdDate <= :to
      and exists (
        select articleSummary.id
        from ArticleSummary articleSummary
        where articleSummary.articleId = article.id
          and articleSummary.isApplied = true
      )
    group by keyword.id, keyword.value
    """
  )
  fun countVisibleHeadKeywordsForFeed(
    blogIds: Collection<Long>,
    from: LocalDate,
    to: LocalDate,
  ): List<VisibleHeadKeywordCount>

  interface HeadKeywordMappingCount {
    val headKeywordId: Long
    val mappingCount: Long
  }

  interface VisibleHeadKeywordCount {
    val keywordValue: String
    val mappingCount: Long
  }
}
