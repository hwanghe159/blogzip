package com.blogzip.api.dto

import com.blogzip.dto.SubscriptionAndBlog
import java.time.LocalDateTime

data class SubscriptionResponse private constructor(
  val id: Long,
  val blog: BlogResponse,
  val createdAt: LocalDateTime,
) {
  companion object {
    fun from(subscriptionAndBlog: SubscriptionAndBlog): SubscriptionResponse {
      return SubscriptionResponse(
        id = subscriptionAndBlog.subscription.id!!,
        blog = BlogResponse.from(subscriptionAndBlog.blog),
        createdAt = subscriptionAndBlog.subscription.createdAt,
      )
    }
  }
}
