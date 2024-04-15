package com.blogzip.api.controller

import com.blogzip.api.dto.*
import com.blogzip.common.DomainException
import com.blogzip.common.ErrorCode
import com.blogzip.notification.email.EmailSender
import com.blogzip.service.AuthService
import com.blogzip.service.UserService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class UserController(
    private val userService: UserService,
    private val authService: AuthService,
    private val emailSender: EmailSender,
) {

    @PostMapping("/api/v1/user")
    fun add(@RequestBody @Valid request: UserCreateRequest): ResponseEntity<Void> {
        val email = request.email
        val user = userService.findByEmail(email)
        val verificationCode = RandomCode(length = 20).value
        if (user == null) {
            userService.save(email, verificationCode)
            emailSender.sendVerification(email, verificationCode)
            return ResponseEntity.ok().build()
        }
        if (user.isVerified) {
            throw DomainException(ErrorCode.ALREADY_VERIFIED)
        }
        if (!user.isVerificationCodeExpired()) {
            throw DomainException(ErrorCode.ALREADY_SENT_VERIFICATION_EMAIL)
        }
        user.refreshVerificationCode(verificationCode)
        emailSender.sendVerification(email, verificationCode)
        return ResponseEntity.ok().build()
    }

    @GetMapping("/api/v1/email/{emailAddress}/verify/{code}")
    fun verifyEmail(
        @PathVariable emailAddress: String,
        @PathVariable code: String
    ): ResponseEntity<Void> {
        userService.verify(emailAddress, code)
        return ResponseEntity.ok().build()
    }

    @PostMapping("/api/v1/login")
    fun login(@RequestBody @Valid request: LoginRequest): ResponseEntity<LoginResponse> {
        val token = authService.login(request.email, request.password)
        return ResponseEntity.ok(
            LoginResponse(
                token.accessToken,
                token.refreshToken
            )
        )
    }
}