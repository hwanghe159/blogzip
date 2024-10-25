package com.blogzip.dto

import com.blogzip.domain.Article
import com.blogzip.domain.ReadLater

data class ReadLaterAndArticle(
    val readLater: ReadLater,
    val article: Article,
) {
}