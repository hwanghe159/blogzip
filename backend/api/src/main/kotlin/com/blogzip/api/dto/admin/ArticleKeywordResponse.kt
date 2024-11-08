package com.blogzip.api.dto.admin

import java.time.LocalDateTime

data class ArticleKeywordResponse private constructor(
    val id: Long,
    val keywordId: Long,
    val createdAt: LocalDateTime,
) {
    companion object {

    }
}