package com.blogzip.api.controller

import com.blogzip.api.dto.RandomCode
import com.blogzip.api.dto.UserCreateRequest
import com.blogzip.notification.email.EmailSender
import com.blogzip.service.UserService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

@RestController
class UserController(
    private val userService: UserService,
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
        if (!user.isVerificationCodeExpired()) {
            throw RuntimeException("이메일을 이미 보냄")
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
}