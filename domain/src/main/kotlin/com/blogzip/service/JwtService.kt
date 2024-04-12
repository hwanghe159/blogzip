package com.blogzip.service

import com.blogzip.domain.User
import com.blogzip.dto.JwtProperties
import com.blogzip.dto.UserToken
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.Jwts.SIG.HS256
import org.springframework.stereotype.Component
import java.util.*


@Component
class JwtService(
    private val jwtProperties: JwtProperties,
) {

    // todo secret-key 적용 필요
    fun createToken(user: User): UserToken {
        val accessToken = Jwts.builder()
            .header()
            .type("JWT")
            .and()
            .subject(user.email)
            .expiration(Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * jwtProperties.accessTokenExpiresDays))
            .signWith(HS256.key().build())
            .compact()

        val refreshToken = Jwts.builder()
            .subject(user.email)
            .expiration(Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * jwtProperties.refreshTokenExpiresDays))
            .signWith(HS256.key().build())
            .compact()
        return UserToken(accessToken, refreshToken)
    }

    fun verifyToken(token: String): Boolean {
        return true
    }
}
