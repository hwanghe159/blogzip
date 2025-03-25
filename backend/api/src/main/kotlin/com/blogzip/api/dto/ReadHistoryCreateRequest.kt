package com.blogzip.api.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class ReadHistoryCreateRequest(

  @JsonProperty("articleUrl")
  val articleUrl: String,
)
