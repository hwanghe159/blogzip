package com.blogzip.api.common

import com.blogzip.api.config.OAuth2Properties
import com.blogzip.api.dto.LoginResponse
import com.blogzip.common.DomainException
import com.blogzip.common.ErrorCode
import com.blogzip.domain.ReceiveDaysConverter
import com.blogzip.domain.SocialType
import com.blogzip.domain.User
import com.blogzip.slack.SlackSender
import com.blogzip.slack.SlackSender.SlackChannel.*
import com.blogzip.service.UserService
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import java.time.DayOfWeek

@Component
class GoogleAuthService(
    private val googleAuthWebClient: WebClient,
    private val googleWebClient: WebClient,
    private val oAuth2Properties: OAuth2Properties,
    private val jwtService: JwtService,
    private val userService: UserService,
    private val slackSender: SlackSender,
) {
    fun googleLogin(code: String): LoginResponse {
        val googleAccessToken = convertToGoogleAccessToken(code)
            ?: throw DomainException(ErrorCode.LOGIN_FAILED)
        val googleUserInfo = getGoogleUserInfo(googleAccessToken)
            ?: throw DomainException(ErrorCode.LOGIN_FAILED)

        val googleId = googleUserInfo.sub
        val email = googleUserInfo.email
        var user = userService.findByEmail(email)
        if (user != null) {
            user.updateGoogleId(googleId)
            userService.save(user)
        } else {
            user = userService.save(
                User(
                    email = email,
                    socialType = SocialType.GOOGLE,
                    socialId = googleId,
                    receiveDays = ReceiveDaysConverter.toString(DayOfWeek.entries),
                )
            )
            slackSender.sendMessageAsync(
                MONITORING,
                "회원가입 발생! email=${email}"
            )
        }
        val accessToken = jwtService.createToken(user)
        return LoginResponse(
            id = user.id!!,
            accessToken = accessToken,
            email = email,
            image = googleUserInfo.picture,
        )
    }

    private fun convertToGoogleAccessToken(code: String): String? {
        val response = googleAuthWebClient.post()
            .uri("/token")
            .bodyValue(
                GoogleTokenRequest(
                    clientId = oAuth2Properties.google.clientId,
                    clientSecret = oAuth2Properties.google.clientSecret,
                    code = code,
                    grantType = "authorization_code",
                    redirectUri = oAuth2Properties.google.redirectUri,
                )
            )
            .retrieve()
            .bodyToMono(GoogleResponse::class.java)
            .block()
        if (response == null) {
            return null
        }
        return response.accessToken
    }

    private fun getGoogleUserInfo(accessToken: String): GoogleUserInfoResponse? {
        return googleWebClient.get()
            .uri("/oauth2/v3/userinfo")
            .headers { it.setBearerAuth(accessToken) }
            .retrieve()
            .bodyToMono(GoogleUserInfoResponse::class.java)
            .block()
    }

    data class GoogleTokenRequest(
        val code: String,
        val clientId: String,
        val clientSecret: String,
        val redirectUri: String,
        val grantType: String,
    )

    data class GoogleResponse(
        val accessToken: String,
        val expiresIn: Int,
        val refreshToken: String?,
        val scope: String,
        val tokenType: String,
        val idToken: String,
    )

    data class GoogleUserInfoResponse(
        val sub: String,
        val email: String,
        val emailVerified: Boolean,
        val familyName: String,
        val givenName: String,

        /**
         * 성(familyName) + 이름(givenName)
         */
        val name: String,
        val picture: String,
    )
}