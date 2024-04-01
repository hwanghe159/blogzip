package com.blogzip.api.dto

import jakarta.validation.constraints.Email

data class UserCreateRequest(

    @field:Email
    val email: String,
)
