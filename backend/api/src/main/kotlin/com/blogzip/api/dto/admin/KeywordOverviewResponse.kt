package com.blogzip.api.dto.admin

import com.blogzip.dto.KeywordOverview
import java.time.LocalDateTime

data class KeywordOverviewResponse(
  val totalKeywordCount: Int,
  val headKeywordCount: Int,
  val followerKeywordCount: Int,
  val visibleKeywordCount: Int,
  val hiddenKeywordCount: Int,
  val headKeywords: List<HeadKeywordOverviewResponse>,
) {
  companion object {
    fun from(keywordOverview: KeywordOverview): KeywordOverviewResponse {
      return KeywordOverviewResponse(
        totalKeywordCount = keywordOverview.totalKeywordCount,
        headKeywordCount = keywordOverview.headKeywordCount,
        followerKeywordCount = keywordOverview.followerKeywordCount,
        visibleKeywordCount = keywordOverview.visibleKeywordCount,
        hiddenKeywordCount = keywordOverview.hiddenKeywordCount,
        headKeywords = keywordOverview.headKeywords.map { headKeyword ->
          HeadKeywordOverviewResponse(
            id = headKeyword.id,
            value = headKeyword.value,
            isVisible = headKeyword.isVisible,
            articleCount = headKeyword.articleCount,
            createdAt = headKeyword.createdAt,
            followers = headKeyword.followers.map { followerKeyword ->
              FollowerKeywordOverviewResponse(
                id = followerKeyword.id,
                value = followerKeyword.value,
                isVisible = followerKeyword.isVisible,
                createdAt = followerKeyword.createdAt,
              )
            }
          )
        }
      )
    }
  }
}

data class HeadKeywordOverviewResponse(
  val id: Long,
  val value: String,
  val isVisible: Boolean,
  val articleCount: Int,
  val createdAt: LocalDateTime,
  val followers: List<FollowerKeywordOverviewResponse>,
)

data class FollowerKeywordOverviewResponse(
  val id: Long,
  val value: String,
  val isVisible: Boolean,
  val createdAt: LocalDateTime,
)
