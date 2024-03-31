package com.blogzip.domain

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDate

interface ArticleRepository : JpaRepository<Article, Long> {

    fun existsByUrl(url: String): Boolean

    @Query("""
        select a 
        from Article a 
        join a.blog b 
        join b.subscriptions s 
        where s.user = :user 
        and a.createdDate = :createdDate
    """)
    fun findAllByUserAndCreatedDate(user: User, createdDate: LocalDate): List<Article>
}