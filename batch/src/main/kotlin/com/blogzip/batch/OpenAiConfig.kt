package com.blogzip.batch

import com.blogzip.batch.summarize.OpenAiProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(OpenAiProperties::class)
class OpenAiConfig {
}