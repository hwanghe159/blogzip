package com.blogzip.batch.summarize

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "open-ai")
data class OpenAiProperties(
    val apiKey: String,
    val assistantId: String,
    val threadId: String
)