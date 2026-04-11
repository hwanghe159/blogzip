package com.blogzip.api.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class FineTuningRequest @JsonCreator(mode = JsonCreator.Mode.PROPERTIES) constructor(
  @JsonProperty("tunedSummary")
  val tunedSummary: String = "",
  @JsonProperty("keywords")
  val keywords: List<String> = emptyList(),
)
