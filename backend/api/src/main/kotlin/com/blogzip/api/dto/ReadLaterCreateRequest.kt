package com.blogzip.api.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class ReadLaterCreateRequest @JsonCreator(mode = JsonCreator.Mode.PROPERTIES) constructor(

  @JsonProperty("articleId")
  val articleId: Long = 0,
)
