package com.blogzip.batch.fetch

import com.blogzip.domain.Article
import com.blogzip.domain.Blog

data class FetchArticlesResult(
  val articles: List<Article>,
  val failures: List<FetchFailure> = emptyList(),
)

data class FetchFailure(
  val blogId: Long?,
  val blogUrl: String,
  val rssStatus: Blog.RssStatus,
  val reason: String,
  val detail: String? = null,
) {
  fun toSlackLine(): String {
    val suffix = if (detail.isNullOrBlank()) "" else " (detail=$detail)"
    return "- url=$blogUrl, blogId=${blogId ?: "unknown"}, rssStatus=$rssStatus, reason=$reason$suffix"
  }
}
