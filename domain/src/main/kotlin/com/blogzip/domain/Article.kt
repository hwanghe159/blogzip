package com.blogzip.domain

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
class Article(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blog_id")
    val blog: Blog,

    val title: String,

    val content: String,

    val url: String,

    var summary: String? = null,

    var summarizedBy: String? = null,

    var createdDate: LocalDate? = null,
) {
    @PrePersist
    fun prePersist() {
        if (createdDate == null) {
            createdDate = LocalDate.now()
        }
    }
}