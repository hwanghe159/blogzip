package com.blogzip.api.dto.admin

import java.time.LocalDateTime

data class HeadKeywordResponse(
  val id: Long,
  val value: String,
  val followers: List<FollowerKeywordResponse>,
  val createdAt: LocalDateTime,
) {
}