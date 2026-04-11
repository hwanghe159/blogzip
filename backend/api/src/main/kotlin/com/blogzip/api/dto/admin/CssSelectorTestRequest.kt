package com.blogzip.api.dto.admin

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class CssSelectorTestRequest @JsonCreator(mode = JsonCreator.Mode.PROPERTIES) constructor(
  @JsonProperty("blogUrl")
  val blogUrl: String = "",
  @JsonProperty("cssSelector")
  val cssSelector: String = "",
  @JsonProperty("sampleSize")
  val sampleSize: Int = 5,
)

data class CssSelectorTestResponse(
  val success: Boolean,
  val blogUrl: String,
  val cssSelector: String,
  val matchedElementCount: Int,
  val extractableUrlCount: Int,
  val sampleMatches: List<CssSelectorTestMatchResponse>,
  val message: String?,
)

data class CssSelectorTestMatchResponse(
  val title: String,
  val url: String,
)
