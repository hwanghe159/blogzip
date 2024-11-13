package com.blogzip.api.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class BlogCreateRequest(
    /**
     * data class 의 필드가 1개인 경우 기본 생성자를 만드는 전략을 선택할 수 없어 MismatchedInputException 발생.
     * `@JsonProperty` 로 해결
     * @see [링크](https://github.com/FasterXML/jackson-databind/issues/3085)
     */
    @JsonProperty("url")
    val url: String,
)
