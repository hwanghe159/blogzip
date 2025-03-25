package com.blogzip.api.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class ReadLaterCreateRequest(

  @JsonProperty("articleId")
  val articleId: Long,
)
