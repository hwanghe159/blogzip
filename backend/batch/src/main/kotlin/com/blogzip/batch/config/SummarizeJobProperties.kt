package com.blogzip.batch.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "spring.batch.job.summarize")
data class SummarizeJobProperties(
  val retry: RetryProperties
) {
  data class RetryProperties(
    val maxAttempts: Int,
    val backoffSeconds: Int,
  )
}