package com.blogzip.api.dto.admin

import com.blogzip.service.ArticleVisibilityUpdateResult
import com.fasterxml.jackson.annotation.JsonProperty

data class ArticleVisibilityUpdateResponse(
  val articleId: Long,
  @get:JsonProperty("beforeIsVisible")
  val beforeIsVisible: Boolean,
  @get:JsonProperty("isVisible")
  val isVisible: Boolean,
) {
  companion object {
    fun from(result: ArticleVisibilityUpdateResult): ArticleVisibilityUpdateResponse {
      return ArticleVisibilityUpdateResponse(
        articleId = result.articleId,
        beforeIsVisible = result.beforeIsVisible,
        isVisible = result.isVisible,
      )
    }
  }
}
