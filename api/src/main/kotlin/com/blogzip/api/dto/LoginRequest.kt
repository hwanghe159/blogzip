package com.blogzip.api.dto

import jakarta.validation.constraints.Email

data class LoginRequest(
    @field:Email
    val email: String,
    val password: String,
)
