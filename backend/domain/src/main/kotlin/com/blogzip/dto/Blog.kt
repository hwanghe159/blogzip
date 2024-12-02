package com.blogzip.dto

import com.blogzip.domain.Blog.RssStatus

data class Blog(
    val id: Long,
    val name: String,
    val url: String,
    val image: String?,
    val rssStatus: RssStatus,
    val rss: String?,
    val urlCssSelector: String?,
    val isShowOnMain: Boolean,
    val createdBy: Long,
) {
    companion object {
        fun from(blog: com.blogzip.domain.Blog): Blog {
            return Blog(
                id = blog.id!!,
                name = blog.name,
                url = blog.url,
                image = blog.image,
                rssStatus = blog.rssStatus,
                rss = blog.rss,
                urlCssSelector = blog.urlCssSelector,
                isShowOnMain = blog.isShowOnMain,
                createdBy = blog.createdBy,
            )
        }
    }
}