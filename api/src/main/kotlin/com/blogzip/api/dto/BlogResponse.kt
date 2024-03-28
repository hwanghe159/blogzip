package com.blogzip.api.dto

import java.time.LocalDateTime

data class BlogResponse(
    val id: Long,
    val name: String,
    val url: String,
    val createdBy: Long,
    val createdAt: LocalDateTime,
)