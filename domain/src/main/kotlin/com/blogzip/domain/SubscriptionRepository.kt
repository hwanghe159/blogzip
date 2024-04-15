package com.blogzip.domain

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface SubscriptionRepository : JpaRepository<Subscription, Long> {

    @Query(
        """
        select s
        from Subscription s join fetch s.blog
        where s.user.id = :userId
    """
    )
    fun findByUserId(userId: Long): List<Subscription>
}