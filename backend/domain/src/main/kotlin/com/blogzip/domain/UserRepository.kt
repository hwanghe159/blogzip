package com.blogzip.domain

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.DayOfWeek

interface UserRepository : JpaRepository<User, Long> {
  fun findByEmail(email: String): User?
  fun findByEmailAndIsDeletedFalse(email: String): User?

  fun findByIdAndIsDeletedFalse(id: Long): User?

  fun findBySocialTypeAndSocialId(socialType: SocialType, socialId: String): User?
  fun findBySocialTypeAndSocialIdAndIsDeletedFalse(socialType: SocialType, socialId: String): User?

  fun findAllByIsDeletedFalse(): List<User>

  @Query(
    """
        select user 
        from User user left join fetch user.subscriptions 
        where user.id = :userId
        and user.isDeleted = false
    """
  )
  fun findByIdWithSubscriptions(userId: Long): User?

  @Query(
    """
        select user
        from User user
        where user.receiveDays like %:dayOfWeek%
        and user.isDeleted = false
    """
  )
  fun findAllByDayOfWeek(dayOfWeek: DayOfWeek): List<User>
}
