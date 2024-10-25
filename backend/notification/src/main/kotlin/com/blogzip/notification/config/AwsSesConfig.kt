package com.blogzip.notification.config

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(AwsSesProperties::class)
class AwsSesConfig