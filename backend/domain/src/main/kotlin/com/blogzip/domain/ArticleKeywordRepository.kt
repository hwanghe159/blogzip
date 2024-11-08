package com.blogzip.domain

import org.springframework.data.jpa.repository.JpaRepository

interface ArticleKeywordRepository : JpaRepository<ArticleKeyword, Long> {

    fun findAllByArticleId(articleId: Long): List<ArticleKeyword>
}