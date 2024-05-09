package com.blogzip.domain

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

    fun findAllByCreatedDateAndSummaryIsNull(createdDate: LocalDate): List<Article>
}