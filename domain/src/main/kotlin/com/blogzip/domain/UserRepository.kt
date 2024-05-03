package com.blogzip.domain

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface UserRepository : JpaRepository<User, Long> {
    fun findByEmail(email: String): User?

    @Query(
        """
        select user
        from User user 
        join fetch user.subscriptions subscription
        join fetch subscription.blog
        where user.id = :userId
    """
    )
    fun findByIdWithSubscriptions(userId: Long): User?
}