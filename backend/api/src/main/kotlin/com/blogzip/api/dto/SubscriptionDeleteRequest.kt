package com.blogzip.api.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class SubscriptionDeleteRequest @JsonCreator(mode = JsonCreator.Mode.PROPERTIES) constructor(

  @JsonProperty("blogId")
  val blogId: Long = 0,
)
