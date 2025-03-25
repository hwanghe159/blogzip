package com.blogzip.service

import com.blogzip.common.DomainException
import com.blogzip.common.ErrorCode
import com.blogzip.domain.BlogRepository
import com.blogzip.domain.UserRepository
import com.blogzip.dto.SubscriptionAndBlog
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SubscriptionCommandService(
  private val userRepository: UserRepository,
  private val blogRepository: BlogRepository,
) {

  @Transactional
  fun save(userId: Long, blogId: Long): SubscriptionAndBlog {
    val user = userRepository.findByIdWithSubscriptions(userId)
      ?: throw DomainException(ErrorCode.USER_NOT_FOUND)
    val blog = blogRepository.findById(blogId)
      .orElseThrow { DomainException(ErrorCode.BLOG_NOT_FOUND) }
    val subscription = user.addSubscription(blogId)
    return SubscriptionAndBlog(subscription, blog)
  }

  @Transactional
  fun delete(userId: Long, blogId: Long): Boolean {
    val user = userRepository.findByIdWithSubscriptions(userId)
      ?: throw DomainException(ErrorCode.USER_NOT_FOUND)
    return user.deleteSubscription(blogId)
  }
}