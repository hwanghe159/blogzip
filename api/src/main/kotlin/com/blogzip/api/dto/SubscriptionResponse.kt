package com.blogzip.api.dto

import java.time.LocalDateTime

data class SubscriptionResponse(
    val id: Long,
    val blog: BlogResponse,
    val createdAt: LocalDateTime,
)
