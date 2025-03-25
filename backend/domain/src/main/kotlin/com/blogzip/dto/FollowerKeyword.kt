package com.blogzip.dto

import com.blogzip.domain.Keyword
import java.time.LocalDateTime

data class FollowerKeyword(
  val id: Long,
  val value: String,
  val createdAt: LocalDateTime,
) {
  companion object {
    fun from(keyword: Keyword): FollowerKeyword {
      return FollowerKeyword(
        id = keyword.id!!,
        value = keyword.value,
        createdAt = keyword.createdAt,
      )
    }
  }
}