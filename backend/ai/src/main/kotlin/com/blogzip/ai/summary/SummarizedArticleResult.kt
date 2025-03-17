package com.blogzip.ai.summary

data class SummarizedArticleResult(
  val result: Result,
  val article: SummarizedArticle?
) {
  enum class Result {
    SUCCESS,
    FAIL
  }
}