package com.blogzip.api.dto

import com.blogzip.domain.Blog
import java.time.LocalDateTime

data class BlogResponse(
    val id: Long,
    val name: String,
    val url: String,
    val rss: String?,
    val createdBy: Long,
    val createdAt: LocalDateTime,
) {
    companion object {
        fun from(blog: Blog): BlogResponse {
            return BlogResponse(
                id = blog.id!!,
                name = blog.name,
                url = blog.url,
                rss = blog.rss,
                createdBy = blog.createdBy,
                createdAt = blog.createdAt,
            )
        }
    }
}