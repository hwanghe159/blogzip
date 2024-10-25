package com.blogzip.domain

import jakarta.persistence.*

@Entity
class FineTuning(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    val articleId: Long,

    var summary: String,
) {
    fun update(tunedSummary: String): FineTuning {
        this.summary = tunedSummary
        return this
    }
}