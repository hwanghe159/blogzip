package com.blogzip.domain

import org.springframework.data.jpa.repository.JpaRepository

interface ArticleRepository : JpaRepository<Article, Long> {

    fun existsByUrl(url: String): Boolean
}