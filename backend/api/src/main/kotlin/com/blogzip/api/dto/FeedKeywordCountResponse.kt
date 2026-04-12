package com.blogzip.api.dto

import com.blogzip.service.FeedKeywordCount

data class FeedKeywordCountResponse(
  val keyword: String,
  val count: Int,
) {
  companion object {
    fun from(feedKeywordCount: FeedKeywordCount): FeedKeywordCountResponse {
      return FeedKeywordCountResponse(
        keyword = feedKeywordCount.keyword,
        count = feedKeywordCount.count,
      )
    }
  }
}
