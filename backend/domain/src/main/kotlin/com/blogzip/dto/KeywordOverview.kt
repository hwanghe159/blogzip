package com.blogzip.dto

import java.time.LocalDateTime

data class KeywordOverview(
  val totalKeywordCount: Int,
  val headKeywordCount: Int,
  val followerKeywordCount: Int,
  val visibleKeywordCount: Int,
  val hiddenKeywordCount: Int,
  val headKeywords: List<HeadKeywordOverview>,
) {
  data class HeadKeywordOverview(
    val id: Long,
    val value: String,
    val isVisible: Boolean,
    val articleCount: Int,
    val createdAt: LocalDateTime,
    val followers: List<FollowerKeywordOverview>,
  )

  data class FollowerKeywordOverview(
    val id: Long,
    val value: String,
    val isVisible: Boolean,
    val createdAt: LocalDateTime,
  )
}
