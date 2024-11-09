package com.blogzip.dto

import com.blogzip.domain.Keyword
import java.time.LocalDateTime

data class HeadKeyword private constructor(
    val id: Long,
    val value: String,
    val followers: List<FollowerKeyword>,
    val createdAt: LocalDateTime,
) {
    companion object {
        fun from(keyword: Keyword): HeadKeyword {
            return HeadKeyword(
                id = keyword.id!!,
                value = keyword.value,
                followers = keyword.followers.map { FollowerKeyword.from(it) },
                createdAt = keyword.createdAt,
            )
        }
    }
}