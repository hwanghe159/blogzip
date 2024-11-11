package com.blogzip.crawler.vo

import java.net.URI

data class MediumUrl(val url: String) {
    // https://medium.com/{path} -> https://medium.com/feed/{path}
    fun rssUrl(): String {
        val url = URI.create(url)
        return "https://medium.com/feed" + url.path
    }

    companion object {
        fun isMediumUrl(url: String): Boolean {
            return URI.create(url).toURL().host == "medium.com"
        }
    }
}