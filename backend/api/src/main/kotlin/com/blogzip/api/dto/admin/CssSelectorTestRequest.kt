package com.blogzip.api.dto.admin

data class CssSelectorTestRequest(
  val blogUrl: String,
  val cssSelector: String,
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
