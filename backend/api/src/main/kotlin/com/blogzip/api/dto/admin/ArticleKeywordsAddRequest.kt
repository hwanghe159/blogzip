package com.blogzip.api.dto.admin

import com.fasterxml.jackson.annotation.JsonProperty

data class ArticleKeywordsAddRequest(

    @JsonProperty("values")
    val values: List<String>,
)
