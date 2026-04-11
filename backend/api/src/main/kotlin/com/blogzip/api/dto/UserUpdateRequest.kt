package com.blogzip.api.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class UserUpdateRequest @JsonCreator(mode = JsonCreator.Mode.PROPERTIES) constructor(

  @JsonProperty("receiveDays")
  val receiveDays: List<String> = emptyList(),
)
