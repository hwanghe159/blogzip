package com.blogzip.crawler.vo

import java.net.URI

data class VelogUrl(val url: String) {
    // https://velog.io/@{user_name}/posts -> https://v2.velog.io/rss/{user_name}
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