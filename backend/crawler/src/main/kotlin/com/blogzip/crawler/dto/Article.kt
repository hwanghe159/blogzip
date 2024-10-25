package com.blogzip.crawler.dto

import java.time.LocalDate

data class Article(
    val title: String,
    val content: String?,
    val url: String,
    val createdDate: LocalDate?
)