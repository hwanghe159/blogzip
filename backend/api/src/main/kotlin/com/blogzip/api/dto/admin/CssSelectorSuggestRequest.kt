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
  @JsonProperty("firstArticleTitle")
  val firstArticleTitle: String = "",
  @JsonProperty("firstArticleUrl")
  val firstArticleUrl: String = "",
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
  val baseConfidence: Double,
  val hintScore: Double,
  val firstArticleTitleMatched: Boolean,
  val firstArticleUrlMatched: Boolean,
  val matchedElementCount: Int,
  val extractableUrlCount: Int,
  val internalUrlCount: Int,
  val sampleMatches: List<CssSelectorTestMatchResponse>,
)
