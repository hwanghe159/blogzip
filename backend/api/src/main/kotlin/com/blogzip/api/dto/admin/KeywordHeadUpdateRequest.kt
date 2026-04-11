package com.blogzip.api.dto.admin

import com.blogzip.service.KeywordHeadUpdateResult
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class KeywordHeadUpdateRequest @JsonCreator(mode = JsonCreator.Mode.PROPERTIES) constructor(
  @JsonProperty("headKeywordId")
  val headKeywordId: Long? = null,
)

data class KeywordHeadUpdateResponse(
  val keywordId: Long,
  val beforeHeadKeywordId: Long?,
  val afterHeadKeywordId: Long?,
) {
  companion object {
    fun from(result: KeywordHeadUpdateResult): KeywordHeadUpdateResponse {
      return KeywordHeadUpdateResponse(
        keywordId = result.keywordId,
        beforeHeadKeywordId = result.beforeHeadKeywordId,
        afterHeadKeywordId = result.afterHeadKeywordId,
      )
    }
  }
}
