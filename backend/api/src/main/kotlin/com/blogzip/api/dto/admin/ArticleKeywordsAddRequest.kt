package com.blogzip.api.dto.admin

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class ArticleKeywordsAddRequest @JsonCreator(mode = JsonCreator.Mode.PROPERTIES) constructor(

  @JsonProperty("values")
  val values: List<String> = emptyList(),
)
