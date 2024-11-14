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
}