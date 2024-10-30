package com.blogzip.crawler.service

interface WebScrapper {

    fun getMetadata(url: String): BlogMetadata
    fun getContent(url: String): String?
    fun getArticles(blogUrl: String, cssSelector: String, articleUrls: Set<String>): ScrapResult
    fun test(url: String): String?
    fun endUse()

    data class Article(
        val title: String,
        val url: String,
        val content: String,
    )

    data class ScrapResult(
        val articles: List<Article>,
        val failCause: Exception?,
    ) {
        fun isFailed(): Boolean {
            return failCause != null
        }
    }

    data class BlogMetadata(
        val title: String,
        val imageUrl: String?,
        val rss: String?,
    )
}