package com.blogzip.domain

import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface ReadLaterRepository : JpaRepository<ReadLater, Long> {

  fun findAllByUserId(userId: Long, sort: Sort?): List<ReadLater>

  fun findByUserIdAndArticleId(userId: Long, articleId: Long): ReadLater?

  @Query(
    """
            select readLater
            from ReadLater readLater
            where readLater.userId = :userId
            and (:next is null or readLater.id <= :next)
        """
  )
  fun search(userId: Long, next: Long?, pageable: Pageable): List<ReadLater>

  fun deleteAllByUserIdAndArticleId(userId: Long, articleId: Long)
}