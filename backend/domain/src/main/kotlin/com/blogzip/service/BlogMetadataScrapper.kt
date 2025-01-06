package com.blogzip.service

interface BlogMetadataScrapper {
    fun getMetadata(url: String): BlogMetadata

    data class BlogMetadata(
        val title: String,
        val imageUrl: String?,
        val rss: String?,

        /**
         * rss != null && rss 내 게시물 글 내용이 포함되어 있으면 true
         * 그 외엔 false
         */
        val isContentContainsInRss: Boolean,
    )
}