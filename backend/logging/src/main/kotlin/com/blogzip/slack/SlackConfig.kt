package com.blogzip.slack

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(SlackProperties::class)
class SlackConfig {
}