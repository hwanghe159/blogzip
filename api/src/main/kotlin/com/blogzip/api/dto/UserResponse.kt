package com.blogzip.api.dto

import com.blogzip.domain.ReceiveDaysConverter
import com.blogzip.domain.User
import java.time.DayOfWeek
import java.time.LocalDateTime

data class UserResponse private constructor(
    val id: Long,
    val email: String,
    var isVerified: Boolean,
    val receiveDays: List<DayOfWeek>,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
) {
    companion object {
        fun from(user: User): UserResponse {
            return UserResponse(
                id = user.id!!,
                email = user.email,
                isVerified = user.isVerified,
                receiveDays = ReceiveDaysConverter.toList(user.receiveDays),
                createdAt = user.createdAt,
                updatedAt = user.updatedAt,
            )
        }
    }
}