package com.blogzip.domain

import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository

interface ReadLaterRepository : JpaRepository<ReadLater, Long> {

    fun findAllByUserId(userId: Long, sort: Sort?): List<ReadLater>

    fun findByUserIdAndArticleId(userId: Long, articleId: Long): ReadLater?

    fun deleteAllByUserIdAndArticleId(userId: Long, articleId: Long)
}