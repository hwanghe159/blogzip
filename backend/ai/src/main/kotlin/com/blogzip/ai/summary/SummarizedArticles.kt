package com.blogzip.ai.summary

data class SummarizedArticle(
  val id: Long,
  val summary: String,
  val keywords: List<String>,
  val summarizedBy: String,
)