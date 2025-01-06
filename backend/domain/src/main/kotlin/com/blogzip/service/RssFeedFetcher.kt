package com.blogzip.service

interface RssFeedFetcher {
    fun getMetadata(url: String): BlogMetadata

    data class BlogMetadata(
        val title: String,
        val imageUrl: String?,
        val rss: String?,
    )
}