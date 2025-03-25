package com.blogzip.api.dto.admin

import com.fasterxml.jackson.annotation.JsonProperty

data class KeywordUpdateRequest(
  @JsonProperty("value")
  val value: String?,
  @JsonProperty("isVisible")
  val isVisible: Boolean?,
)