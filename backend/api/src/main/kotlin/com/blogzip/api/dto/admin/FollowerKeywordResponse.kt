package com.blogzip.api.dto.admin

import java.time.LocalDateTime

data class FollowerKeywordResponse(
    val id: Long,
    val value: String,
    val createdAt: LocalDateTime,
) {
    companion object {

    }
}