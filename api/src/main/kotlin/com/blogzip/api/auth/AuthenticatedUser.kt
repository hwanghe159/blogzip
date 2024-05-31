package com.blogzip.api.auth

import com.blogzip.domain.SocialType
import java.time.LocalDateTime

data class AuthenticatedUser(
    val id: Long,
    val email: String,
    val socialType: SocialType,
    val socialId: String,
    val receiveDays: String,
    var createdAt: LocalDateTime,
    var updatedAt: LocalDateTime,
)