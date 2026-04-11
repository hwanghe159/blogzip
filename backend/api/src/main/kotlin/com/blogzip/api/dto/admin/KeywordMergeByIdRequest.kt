package com.blogzip.api.dto.admin

data class KeywordMergeByIdRequest(
  val srcKeywordId: Long,
  val destKeywordId: Long,
)
