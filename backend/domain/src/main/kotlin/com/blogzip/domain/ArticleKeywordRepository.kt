package com.blogzip.domain

import org.springframework.data.jpa.repository.JpaRepository

interface ArticleKeywordRepository : JpaRepository<ArticleKeyword, Long> {

  fun findAllByArticleId(articleId: Long): List<ArticleKeyword>

  fun findAllByArticleIdIn(articleIds: Collection<Long>): List<ArticleKeyword>

  fun findAllByHeadKeywordId(headKeywordId: Long): List<ArticleKeyword>
}