package com.blogzip.domain

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    val email: String,

    val password: String,

//    val receiveDays: List<DayOfWeek>,

    @OneToMany(mappedBy = "user")
    val subscriptions: List<Subscription>,

    val createdAt: LocalDateTime,
)