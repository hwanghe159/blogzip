package com.blogzip.api.config

import com.blogzip.api.auth.JwtProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(JwtProperties::class, OAuth2Properties::class)
class AuthConfig {
}