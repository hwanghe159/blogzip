package com.blogzip.ai.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "open-ai")
data class OpenAiProperties(
    // todo key 교체
    val apiKey: String,
    val apiKeyNew: String,
    val assistantId: String,
    val threadId: String
)