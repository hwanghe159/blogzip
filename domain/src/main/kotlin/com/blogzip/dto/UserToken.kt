package com.blogzip.dto

data class UserToken(
    val accessToken: String,
    val refreshToken: String, // todo
)
