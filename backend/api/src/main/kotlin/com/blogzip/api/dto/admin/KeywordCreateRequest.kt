package com.blogzip.api.dto.admin

import com.blogzip.domain.Keyword
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotBlank

data class KeywordCreateRequest @JsonCreator(mode = JsonCreator.Mode.PROPERTIES) constructor(
  @JsonProperty("value")
  @field:NotBlank(message = "value는 필수입니다.")
  val value: String = "",
  @JsonProperty("isVisible")
  val isVisible: Boolean = true,
)

data class KeywordCreateResponse(
  val id: Long,
  val value: String,
  @get:JsonProperty("isVisible")
  val isVisible: Boolean,
  val isHead: Boolean,
) {
  companion object {
    fun from(keyword: Keyword): KeywordCreateResponse {
      return KeywordCreateResponse(
        id = keyword.id!!,
        value = keyword.value,
        isVisible = keyword.isVisible,
        isHead = keyword.isHead(),
      )
    }
  }
}
