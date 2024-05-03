package com.blogzip.api.dto

import com.blogzip.domain.Subscription
import java.time.LocalDateTime

data class SubscriptionResponse private constructor(
    val id: Long,
    val blog: BlogResponse,
    val createdAt: LocalDateTime,
) {
    companion object {
        fun from(subscription: Subscription): SubscriptionResponse {
            return SubscriptionResponse(
                id = subscription.id!!,
                blog = BlogResponse.from(subscription.blog),
                createdAt = subscription.createdAt,
            )
        }
    }
}
