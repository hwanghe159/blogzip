package com.blogzip.api.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class ReadHistoryCreateRequest @JsonCreator(mode = JsonCreator.Mode.PROPERTIES) constructor(

  @JsonProperty("articleUrl")
  val articleUrl: String = "",
)
