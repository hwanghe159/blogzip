package com.blogzip.dto

import com.blogzip.domain.Article

data class SearchedArticles(
    val articles: List<Article>,
    val next: Long?,
)