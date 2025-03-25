package com.blogzip.ai.summary

sealed class SummarizedArticleResult(
) {
  data class Success(val article: SummarizedArticle) : SummarizedArticleResult()
  data class Failure(val articleId: Long, val throwable: Throwable) : SummarizedArticleResult()

  fun handle(
    onSuccess: (SummarizedArticle) -> Unit,
    onFailure: (Long, Throwable) -> Unit
  ) {
    when (this) {
      is Success -> onSuccess(article)
      is Failure -> onFailure(articleId, throwable)
    }
  }

  fun isSuccess(): Boolean {
    return when (this) {
      is Success -> true
      is Failure -> false
    }
  }
}