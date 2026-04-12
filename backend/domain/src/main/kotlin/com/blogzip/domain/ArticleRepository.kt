package com.blogzip.domain

import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDate

interface ArticleRepository : JpaRepository<Article, Long> {

  fun existsByUrl(url: String): Boolean

  fun findByUrl(url: String): Article?

  @Query(
    """
            select article
            from Article article
            where article.createdDate >= :createdDate
            and not exists (
                select articleSummary.id
                from ArticleSummary articleSummary
                where articleSummary.articleId = article.id
                and articleSummary.isApplied = true
            )
        """
  )
  fun findAllByCreatedDateGreaterThanEqualAndNoAppliedSummary(createdDate: LocalDate): List<Article>

  @Query(
    """
            select article
            from Article article
            where (:next is null or article.id < :next)
        """
  )
  fun searchRecent(
    next: Long?,
    pageable: Pageable,
  ): List<Article>

  @Query(
    """
            select article
            from Article article
            where article.blogId in :blogIds
            and article.createdDate >= :from
            and article.createdDate <= :to
            and (:next is null or article.id < :next)
            and exists (
                select articleSummary.id
                from ArticleSummary articleSummary
                where articleSummary.articleId = article.id
                and articleSummary.isApplied = true
            )
        """
  )
  fun search(
    blogIds: Collection<Long>,
    from: LocalDate,
    to: LocalDate,
    next: Long?,
    pageable: Pageable
  ): List<Article>

  @Query(
    """
            select article
            from Article article
            where article.blogId in :blogIds
            and (:next is null or article.id < :next)
            and article.id in (
                select articleKeyword.articleId
                from ArticleKeyword articleKeyword
                where articleKeyword.headKeywordId in :headKeywordIds
            )
            and exists (
                select articleSummary.id
                from ArticleSummary articleSummary
                where articleSummary.articleId = article.id
                and articleSummary.isApplied = true
            )
        """
  )
  fun searchByHeadKeywords(
    headKeywordIds: Collection<Long>,
    blogIds: Collection<Long>,
    next: Long?,
    pageable: Pageable,
  ): List<Article>

  fun findAllByBlogIdInAndCreatedDateIn(
    blogIds: Collection<Long>,
    createdDates: Collection<LocalDate>
  ): List<Article>

  fun findAllByBlogId(blogId: Long): List<Article>
}
