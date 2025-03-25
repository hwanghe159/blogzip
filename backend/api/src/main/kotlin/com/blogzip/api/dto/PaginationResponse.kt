package com.blogzip.api.dto

data class PaginationResponse<T>(
  val items: List<T>,
  val next: Long?,
)