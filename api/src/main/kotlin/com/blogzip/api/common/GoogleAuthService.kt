package com.blogzip.api.common

import com.blogzip.api.config.OAuth2Properties
import com.blogzip.common.DomainException
import com.blogzip.common.ErrorCode
import com.blogzip.domain.User
import com.blogzip.dto.UserToken
import com.blogzip.service.UserService
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component
class GoogleAuthService(
    private val googleAuthWebClient: WebClient,
    private val oAuth2Properties: OAuth2Properties,
    private val jwtService: JwtService,
    private val userService: UserService,
) {

    fun googleLogin(code: String): UserToken {
        val response = googleAuthWebClient.post()
            .uri("/token")
//            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(
                """
            {
                "code": "${code}",
                "client_id": "${oAuth2Properties.google.clientId}",
                "client_secret": "${oAuth2Properties.google.clientSecret}",
                "redirect_uri": "${oAuth2Properties.google.redirectUri}",
                "grant_type": "authorization_code"
            }
        """.trimIndent()
            )
            .retrieve()
            .bodyToMono(GoogleResponse::class.java)
            .block()
        if (response == null) {
            throw DomainException(ErrorCode.LOGIN_FAILED)
        }
        val payload = jwtService.decodePayload(response.idToken)
        val googleId = payload.subject
        var user = userService.findByGoogleId(googleId)
//        if (user == null) {
//            user = userService.save(User(email = payload["email"].toString(), googleId = googleId))
//        } else {
//
//        }
        return UserToken("", "")


    }

    data class GoogleResponse(
        val idToken: String,
    )
}