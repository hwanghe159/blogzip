package com.blogzip.api.dto

import com.blogzip.domain.Blog
import java.time.LocalDateTime

data class BlogResponse private constructor(
    val id: Long,
    val name: String,
    val url: String,
    val image: String?,
    val rssStatus: Blog.RssStatus,
    val rss: String?,
    val createdBy: Long,
    val createdAt: LocalDateTime,
) {
    companion object {
        fun from(blog: com.blogzip.domain.Blog): BlogResponse {
            return BlogResponse(
                id = blog.id!!,
                name = blog.name,
                url = blog.url,
                image = blog.image,
                rss = blog.rss,
                rssStatus = blog.rssStatus,
                createdBy = blog.createdBy,
                createdAt = blog.createdAt,
            )
        }

        fun from(blog: com.blogzip.dto.SearchedArticles.Article.Blog): BlogResponse {
            return BlogResponse(
                id = blog.id,
                name = blog.name,
                url = blog.url,
                image = blog.image,
                rss = blog.rss,
                rssStatus = blog.rssStatus,
                createdBy = blog.createdBy,
                createdAt = blog.createdAt,
            )
        }
    }
}