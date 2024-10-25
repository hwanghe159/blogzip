package com.blogzip.dto

import com.blogzip.domain.Article
import com.blogzip.domain.Blog

data class ArticleAndBlog(
    val article: Article,
    val blog: Blog,
) {
}