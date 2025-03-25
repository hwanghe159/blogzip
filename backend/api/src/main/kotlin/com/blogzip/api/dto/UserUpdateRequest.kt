package com.blogzip.api.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class UserUpdateRequest(

  @JsonProperty("receiveDays")
  val receiveDays: List<String>,
)