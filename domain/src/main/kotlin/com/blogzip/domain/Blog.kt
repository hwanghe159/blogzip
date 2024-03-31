package com.blogzip.domain

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@EntityListeners(AuditingEntityListener::class)
class Blog(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @OneToMany(mappedBy = "blog")
    val subscriptions: List<Subscription> = emptyList(),

    val name: String,

    val url: String,

    @Enumerated(EnumType.STRING)
    val rssStatus: RssStatus,

    val rss: String?,

    val createdBy: Long,

    @CreatedDate
    var createdAt: LocalDateTime = LocalDateTime.MIN,
) {
    enum class RssStatus {
        WITH_CONTENT,
        WITHOUT_CONTENT,
        NO_RSS
    }
}