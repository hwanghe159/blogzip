package com.blogzip.dto

import java.time.LocalDate

data class Article(
    val id: Long,
    val blog: Blog,
    val title: String,
    val content: String,
    val url: String,
    val summary: String,
    val summarizedBy: String,
    val createdDate: LocalDate,
) {
}