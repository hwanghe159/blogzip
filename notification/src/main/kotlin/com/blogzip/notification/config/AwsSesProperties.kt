package com.blogzip.notification.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "aws.ses")
data class AwsSesProperties(
    val accessKey: String,
    val secretKey: String,
)