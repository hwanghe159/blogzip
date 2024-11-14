package com.blogzip.dto

data class SearchedReadLaters(
    val readLaters: List<ReadLater>,
    val next: Long?,
)
