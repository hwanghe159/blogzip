package com.blogzip.api.dto.admin

data class CssSelectorSuggestRequest(
  val blogUrl: String,
  val candidateLimit: Int = 10,
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
