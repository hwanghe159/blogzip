package com.blogzip.api.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "selenium")
data class SeleniumProperties(
    val chromeOptions: List<String>
)
