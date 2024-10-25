package com.blogzip.crawler.vo

import java.net.URI

data class VelogUrl(val url: String) {
    fun rssUrl(): String {
        val userName = URI.create(url).toURL().path.substringAfterLast("@").split("/")[0]
        return "https://v2.velog.io/rss/${userName}"
    }

    companion object {
        fun isVelogUrl(url: String): Boolean {
            return URI.create(url).toURL().host == "velog.io"
        }
    }
}