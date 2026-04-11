package com.blogzip.api.dto.admin

import com.blogzip.service.ArticleCreatedDateUpdateResult
import java.time.LocalDate

data class ArticleCreatedDateUpdateRequest(
  val createdDate: LocalDate,
)

data class ArticleCreatedDateBulkUpdateRequest(
  val items: List<ArticleCreatedDateBulkUpdateItem>,
)

data class ArticleCreatedDateBulkUpdateItem(
  val articleId: Long,
  val createdDate: LocalDate,
)

data class ArticleCreatedDateUpdateResponse(
  val articleId: Long,
  val beforeCreatedDate: LocalDate?,
  val createdDate: LocalDate,
) {
  companion object {
    fun from(result: ArticleCreatedDateUpdateResult): ArticleCreatedDateUpdateResponse {
      return ArticleCreatedDateUpdateResponse(
        articleId = result.articleId,
        beforeCreatedDate = result.beforeCreatedDate,
        createdDate = result.createdDate,
      )
    }
  }
}
