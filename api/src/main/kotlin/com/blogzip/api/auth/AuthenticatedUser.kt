package com.blogzip.api.auth

data class AuthenticatedUser(
    val id: Long,
    val email: String,
)