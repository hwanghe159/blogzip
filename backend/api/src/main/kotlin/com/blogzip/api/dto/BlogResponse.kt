package com.blogzip.api.dto

import com.blogzip.domain.Blog
import com.fasterxml.jackson.annotation.JsonProperty

data class BlogResponse private constructor(
    val id: Long,
    val name: String,
    val url: String,
    val image: String?,
    val rssStatus: Blog.RssStatus,
    val rss: String?,
    @get:JsonProperty("isShowOnMain")
    val isShowOnMain: Boolean,
) {
    companion object {
        fun from(blog: Blog): BlogResponse {
            return BlogResponse(
                id = blog.id!!,
                name = blog.name,
                url = blog.url,
                image = blog.image,
                rss = blog.rss,
                isShowOnMain = blog.isShowOnMain,
                rssStatus = blog.rssStatus,
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
                isShowOnMain = blog.isShowOnMain,
            )
        }

        fun from(blog: com.blogzip.dto.Blog): BlogResponse {
            return BlogResponse(
                id = blog.id,
                name = blog.name,
                url = blog.url,
                image = blog.image,
                rssStatus = blog.rssStatus,
                rss = blog.rss,
                isShowOnMain = blog.isShowOnMain,
            )
        }
    }
}