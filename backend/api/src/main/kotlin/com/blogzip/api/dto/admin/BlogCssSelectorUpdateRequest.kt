package com.blogzip.api.dto.admin

data class BlogCssSelectorUpdateRequest(
  val cssSelector: String,
  val sampleSize: Int = 5,
  val force: Boolean = false,
)

data class BlogCssSelectorUpdateResponse(
  val blogId: Long,
  val blogUrl: String,
  val cssSelector: String,
  val saved: Boolean,
  val testResult: CssSelectorTestResponse,
  val message: String?,
)
