package com.blogzip.dto

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "jwt")
data class JwtProperties(
    val secretKey: String,
    val accessTokenExpiresDays: Int,
    val refreshTokenExpiresDays: Int,
)

