package com.blogzip.notification.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "slack")
data class SlackProperties(
    val webhookUrl: String,
)