package com.blogzip.api.dto.admin

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class CssSelectorSuggestRequest @JsonCreator(mode = JsonCreator.Mode.PROPERTIES) constructor(
  @JsonProperty("blogUrl")
  val blogUrl: String = "",
  @JsonProperty("candidateLimit")
  val candidateLimit: Int = 10,
  @JsonProperty("sampleSize")
  val sampleSize: Int = 5,
)

data class CssSelectorSuggestResponse(
  val success: Boolean,
  val blogUrl: String,
  val candidates: List<CssSelectorCandidateResponse>,
  val message: String?,
)

data class CssSelectorCandidateResponse(
  val selector: String,
  val confidence: Double,
  val matchedElementCount: Int,
  val extractableUrlCount: Int,
  val internalUrlCount: Int,
  val sampleMatches: List<CssSelectorTestMatchResponse>,
)
