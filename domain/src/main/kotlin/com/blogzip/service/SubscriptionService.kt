package com.blogzip.service

import com.blogzip.domain.Subscription
import com.blogzip.domain.SubscriptionRepository
import org.springframework.stereotype.Service

@Service
class SubscriptionService(
    private val repository: SubscriptionRepository,
) {

    fun findByUserId(userId: Long): List<Subscription> {
        return repository.findByUserId(userId)
    }
}