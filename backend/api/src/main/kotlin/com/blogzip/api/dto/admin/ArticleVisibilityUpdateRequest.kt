package com.blogzip.api.dto.admin

import com.fasterxml.jackson.annotation.JsonProperty

data class ArticleVisibilityUpdateRequest(
  @param:JsonProperty("isVisible")
  @get:JsonProperty("isVisible")
  val isVisible: Boolean = true,
)
