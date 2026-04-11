package com.blogzip.api.dto.admin

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class KeywordMergeByIdRequest @JsonCreator(mode = JsonCreator.Mode.PROPERTIES) constructor(
  @JsonProperty("srcKeywordId")
  val srcKeywordId: Long = 0,
  @JsonProperty("destKeywordId")
  val destKeywordId: Long = 0,
)
