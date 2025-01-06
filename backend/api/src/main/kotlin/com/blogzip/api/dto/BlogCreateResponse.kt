package com.blogzip.api.dto

import com.blogzip.domain.Blog
import com.blogzip.service.BlogCreateService.BlogCreateResult

data class BlogCreateResponse private constructor(
    val id: Long,
    val name: String,
    val url: String,
    val image: String?,
    val rss: String?,
) {
    companion object {
        fun from(blog: BlogCreateResult): BlogCreateResponse {
            return BlogCreateResponse(
                id = blog.id,
                name = blog.name,
                url = blog.url,
                image = blog.image,
                rss = blog.rss,
            )
        }
    }
}