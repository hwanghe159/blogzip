package com.blogzip.api.dto.admin

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class BlogCssSelectorUpdateRequest @JsonCreator(mode = JsonCreator.Mode.PROPERTIES) constructor(
  @JsonProperty("cssSelector")
  val cssSelector: String = "",
  @JsonProperty("sampleSize")
  val sampleSize: Int = 5,
  @JsonProperty("force")
  val force: Boolean = false,
)

data class BlogCssSelectorUpdateByUrlRequest @JsonCreator(mode = JsonCreator.Mode.PROPERTIES) constructor(
  @JsonProperty("blogUrl")
  val blogUrl: String = "",
  @JsonProperty("cssSelector")
  val cssSelector: String = "",
  @JsonProperty("sampleSize")
  val sampleSize: Int = 5,
  @JsonProperty("force")
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
