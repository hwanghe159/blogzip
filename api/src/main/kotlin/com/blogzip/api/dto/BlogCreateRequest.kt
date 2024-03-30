package com.blogzip.api.dto

data class BlogCreateRequest(
    val url: String,
    val createdBy: Long, // todo 토큰 기반으로 변경
)
