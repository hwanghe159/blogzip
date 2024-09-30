package com.blogzip.crawler.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "selenium")
data class SeleniumProperties(
    val chromeOptions: List<String>
)
