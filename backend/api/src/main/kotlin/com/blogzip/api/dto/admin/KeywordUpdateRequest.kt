package com.blogzip.api.dto.admin

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class KeywordUpdateRequest @JsonCreator(mode = JsonCreator.Mode.PROPERTIES) constructor(
  @JsonProperty("value")
  val value: String? = null,
  @JsonProperty("isVisible")
  val isVisible: Boolean? = null,
)
