package com.blogzip.domain

import org.springframework.data.jpa.repository.JpaRepository

interface ArticleReportRepository : JpaRepository<ArticleReport, Long> {

  fun findByArticleIdAndUserId(articleId: Long, userId: Long): ArticleReport?
}
