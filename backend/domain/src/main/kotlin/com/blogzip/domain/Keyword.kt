package com.blogzip.domain

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@EntityListeners(AuditingEntityListener::class)
class Keyword(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    val value: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "head_id")
    var head: Keyword? = null,

    @OneToMany(fetch = FetchType.LAZY, cascade = [CascadeType.ALL], mappedBy = "head")
    val followers: List<Keyword> = mutableListOf(),

    @CreatedDate
    var createdAt: LocalDateTime = LocalDateTime.MIN,
) {

    fun isHead(): Boolean {
        return this.followers.isEmpty()
    }

    fun follow(keyword: Keyword): Keyword {
        this.head = keyword
        return this
    }
}