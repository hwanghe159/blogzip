package com.blogzip.api.common

import com.blogzip.api.auth.JwtProperties
import com.blogzip.domain.User
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Component
import java.util.*


@Component
class JwtService(
    private val jwtProperties: JwtProperties,
) {

    private final val key = Keys.hmacShaKeyFor(jwtProperties.secretKey.toByteArray(Charsets.UTF_8))

    fun createToken(user: User): String {
        val now = Date()
        val accessTokenExpiration =
            Date(now.time + jwtProperties.accessTokenExpiresDays * 1000 * 60 * 60 * 24)
        val accessToken = Jwts.builder()
            .header()
            .type("JWT")
            .and()
            .subject(user.email)
            .expiration(accessTokenExpiration)
            .signWith(key, Jwts.SIG.HS256)
            .compact()
        return accessToken
    }

    fun getEmail(token: String): String? {
        return try {
            Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token).payload.subject
        } catch (e: Exception) {
            null
        }
    }

    fun decodePayload(token: String): Claims {
        return Jwts.parser().build().parseSignedClaims(token).payload
    }
}
