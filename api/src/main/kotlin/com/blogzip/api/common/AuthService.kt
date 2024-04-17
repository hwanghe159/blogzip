package com.blogzip.api.common

import com.blogzip.common.DomainException
import com.blogzip.common.ErrorCode
import com.blogzip.dto.UserToken
import com.blogzip.service.UserService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val userService: UserService,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
) {

    fun login(email: String, password: String): UserToken {
        val user = userService.findByEmail(email)
            ?: throw DomainException(ErrorCode.EMAIL_NOT_FOUND)
//        val loginSuccess = passwordEncoder.matches(password, user.password)
//        if (!loginSuccess) {
//            throw DomainException(ErrorCode.LOGIN_FAILED)
//        }
        return jwtService.createToken(user)
    }
}