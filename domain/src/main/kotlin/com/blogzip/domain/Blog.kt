package com.blogzip.domain

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDate
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

    val urlCssSelector: String? = null,

    val createdBy: Long,

    @CreatedDate
    var createdAt: LocalDateTime = LocalDateTime.MIN,
) {

    fun isNew(): Boolean {
        val createdDate = this.createdAt.toLocalDate()
        val yesterday = LocalDate.now().minusDays(1)
        return yesterday <= createdDate
    }

    enum class RssStatus {
        WITH_CONTENT,
        WITHOUT_CONTENT,
        NO_RSS
    }
}