package com.blogzip.dto

import com.blogzip.domain.Article
import com.blogzip.domain.FineTuning

data class FineTuningAndArticle(
  val fineTuning: FineTuning,
  val article: Article,
) {
}