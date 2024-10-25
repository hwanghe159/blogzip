package com.blogzip.api.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "oauth2")
data class OAuth2Properties(
    val google: GoogleOAuth2Properties,
) {
    data class GoogleOAuth2Properties(
        val clientId: String,
        val clientSecret: String,
        val redirectUri: String,
    )
}