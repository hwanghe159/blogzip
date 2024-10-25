package com.blogzip.notification.email

import java.time.LocalDate

data class User(
    val email: String,
    val receiveDates: List<LocalDate>,
)