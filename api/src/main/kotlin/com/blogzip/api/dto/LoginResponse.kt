package com.blogzip.api.dto

data class LoginResponse(
    val id: Long,
    val accessToken: String,
    val email: String,
    val image: String,
)
