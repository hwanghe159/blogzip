package com.blogzip.service

import com.blogzip.common.DomainException
import com.blogzip.common.ErrorCode
import com.blogzip.domain.BlogRepository
import com.blogzip.domain.Subscription
import com.blogzip.domain.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SubscriptionService(
    private val userRepository: UserRepository,
    private val blogRepository: BlogRepository,
) {

    @Transactional(readOnly = true)
    fun findByUserId(userId: Long): List<Subscription> {
        val user = userRepository.findByIdWithSubscriptions(userId)
            ?: throw DomainException(ErrorCode.USER_NOT_FOUND)
        return user.subscriptions
    }

    @Transactional
    fun save(userId: Long, blogId: Long): Subscription {
        val user = userRepository.findByIdWithSubscriptions(userId)
            ?: throw DomainException(ErrorCode.USER_NOT_FOUND)
        val blog = blogRepository.findById(blogId)
            .orElseThrow { DomainException(ErrorCode.BLOG_NOT_FOUND) }
        return user.addSubscription(blog)
    }

    @Transactional
    fun delete(userId: Long, blogId: Long): Boolean {
        val user = userRepository.findByIdWithSubscriptions(userId)
            ?: throw DomainException(ErrorCode.USER_NOT_FOUND)
        return user.deleteSubscription(blogId)
    }
}