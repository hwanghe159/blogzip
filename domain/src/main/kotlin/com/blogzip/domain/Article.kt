package com.blogzip.domain

import jakarta.persistence.*
import java.time.LocalDate

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

    override fun toString(): String {
        return "Article(id=$id, blog=$blog, title='$title', content='$content', url='$url', summary=$summary, summarizedBy=$summarizedBy, createdDate=$createdDate)"
    }
}