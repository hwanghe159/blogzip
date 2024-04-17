package com.blogzip.crawler.confg

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(SeleniumProperties::class)
class SeleniumConfig {
}