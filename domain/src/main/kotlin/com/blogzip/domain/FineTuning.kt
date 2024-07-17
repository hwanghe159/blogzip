package com.blogzip.domain

import jakarta.persistence.*

@Entity
class FineTuning(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id")
    val article: Article,

    var summary: String,
) {
    fun update(tunedSummary: String): FineTuning {
        this.summary = tunedSummary
        return this
    }
}