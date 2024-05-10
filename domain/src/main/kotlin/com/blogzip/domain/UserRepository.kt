package com.blogzip.domain

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.DayOfWeek

interface UserRepository : JpaRepository<User, Long> {
    fun findByEmail(email: String): User?

    fun findByGoogleId(googleId: String): User?

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

    @Query(
        """
        select user
        from User user
        where user.receiveDays like %:dayOfWeek%
    """
    )
    fun findAllByDayOfWeek(dayOfWeek: DayOfWeek): List<User>
}