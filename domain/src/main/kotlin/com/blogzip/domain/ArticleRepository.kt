package com.blogzip.domain

import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDate

interface ArticleRepository : JpaRepository<Article, Long> {

    fun existsByUrl(url: String): Boolean

    @Query(
        """
        select article 
        from Article article 
        join article.blog blog 
        join blog.subscriptions subscriptions 
        where subscriptions.user = :user 
        and article.createdDate = :createdDate
    """
    )
    fun findAllByUserAndCreatedDate(user: User, createdDate: LocalDate): List<Article>

    fun findAllByCreatedDate(createdDate: LocalDate): List<Article>

    fun findAllByCreatedDateIn(createdDates: List<LocalDate>): List<Article>

    fun findAllByCreatedDateGreaterThanEqualAndSummaryIsNull(createdDate: LocalDate): List<Article>

    @Query(
        """
            select article
            from Article article 
            join fetch article.blog blog
            where article.createdDate >= :from
            and article.createdDate <= :to
            and (:next is null or article.id <= :next)
            and article.summary is not null
            and blog.isShowOnMain = true
        """
    )
    fun search(
        from: LocalDate,
        to: LocalDate,
        next: Long?,
        pageable: Pageable
    ): List<Article>

    @Query(
        """
            select article
            from Article article 
            join fetch article.blog blog
            where article.blog in :blogs
            and article.createdDate >= :from
            and article.createdDate <= :to
            and (:next is null or article.id <= :next)
            and article.summary is not null
        """
    )
    fun searchMy(
        blogs: Collection<Blog>,
        from: LocalDate,
        to: LocalDate,
        next: Long?,
        pageable: Pageable
    ): List<Article>
}