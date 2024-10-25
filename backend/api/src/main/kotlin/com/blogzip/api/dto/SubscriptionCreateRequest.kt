package com.blogzip.api.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class SubscriptionCreateRequest(

    @JsonProperty("blogId")
    val blogId: Long,
)
