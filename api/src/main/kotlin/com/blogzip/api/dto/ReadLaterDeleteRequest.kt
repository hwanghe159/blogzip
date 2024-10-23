package com.blogzip.api.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class ReadLaterDeleteRequest(

    @JsonProperty("articleId")
    val articleId: Long,
)
