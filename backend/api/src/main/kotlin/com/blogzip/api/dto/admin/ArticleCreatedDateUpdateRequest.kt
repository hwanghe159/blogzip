package com.blogzip.api.dto.admin

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.blogzip.service.ArticleCreatedDateUpdateResult
import java.time.LocalDate

data class ArticleCreatedDateUpdateRequest @JsonCreator(mode = JsonCreator.Mode.PROPERTIES) constructor(
  @JsonProperty("createdDate")
  val createdDate: LocalDate = LocalDate.MIN,
)

data class ArticleCreatedDateBulkUpdateRequest @JsonCreator(mode = JsonCreator.Mode.PROPERTIES) constructor(
  @JsonProperty("items")
  val items: List<ArticleCreatedDateBulkUpdateItem> = emptyList(),
)

data class ArticleCreatedDateBulkUpdateItem @JsonCreator(mode = JsonCreator.Mode.PROPERTIES) constructor(
  @JsonProperty("articleId")
  val articleId: Long = 0,
  @JsonProperty("createdDate")
  val createdDate: LocalDate = LocalDate.MIN,
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
