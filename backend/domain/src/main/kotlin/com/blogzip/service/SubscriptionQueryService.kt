package com.blogzip.service

import com.blogzip.common.DomainException
import com.blogzip.common.ErrorCode
import com.blogzip.domain.BlogRepository
import com.blogzip.domain.UserRepository
import com.blogzip.dto.SubscriptionAndBlog
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SubscriptionQueryService(
  private val userRepository: UserRepository,
  private val blogRepository: BlogRepository,
) {

  @Transactional(readOnly = true)
  fun findAllByUserId(userId: Long): List<SubscriptionAndBlog> {
    val user = userRepository.findByIdWithSubscriptions(userId)
      ?: throw DomainException(ErrorCode.USER_NOT_FOUND)
    val blogs = blogRepository.findAllById(user.getAllSubscribingBlogIds())
      .map { it.id to it }.toMap()
    return user.subscriptions
      .filter { blogs[it.blogId] != null }
      .map { SubscriptionAndBlog(it, blogs[it.blogId]!!) }
  }
}