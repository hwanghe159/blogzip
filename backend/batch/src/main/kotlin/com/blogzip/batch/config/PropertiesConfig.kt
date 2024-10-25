package com.blogzip.batch.config

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(SummarizeJobProperties::class)
class PropertiesConfig