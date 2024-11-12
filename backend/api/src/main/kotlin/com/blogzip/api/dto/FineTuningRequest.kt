package com.blogzip.api.dto

data class FineTuningRequest(
    val tunedSummary: String,
    val keywords: List<String>,
)
