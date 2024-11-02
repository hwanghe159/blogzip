package com.blogzip.domain

import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDate

interface ArticleRepository : JpaRepository<Article, Long> {

    fun existsByUrl(url: String): Boolean

    fun findByUrl(url: String): Article?

    fun findAllByCreatedDateGreaterThanEqualAndSummaryIsNull(createdDate: LocalDate): List<Article>

    @Query(
        """
            select article
            from Article article
            where article.blogId in :blogIds
            and article.createdDate >= :from
            and article.createdDate <= :to
            and (:next is null or article.id <= :next)
            and article.summary is not null
        """
    )
    fun search(
        blogIds: Collection<Long>,
        from: LocalDate,
        to: LocalDate,
        next: Long?,
        pageable: Pageable
    ): List<Article>

    fun findAllByBlogIdInAndCreatedDateIn(
        blogIds: Collection<Long>,
        createdDates: Collection<LocalDate>
    ): List<Article>

    fun findAllByBlogId(blogId: Long): List<Article>
}