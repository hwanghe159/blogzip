package com.blogzip.api.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class SubscriptionDeleteRequest(

    @JsonProperty("blogId")
    val blogId: Long,
)
