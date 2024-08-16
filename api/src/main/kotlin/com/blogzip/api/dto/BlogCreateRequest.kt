package com.blogzip.api.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class BlogCreateRequest(

    @JsonProperty("url")
    val url: String,
)
