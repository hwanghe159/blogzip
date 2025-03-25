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

  @ElementCollection
  @CollectionTable(name = "article", joinColumns = [JoinColumn(name = "blog_id")])
  @Column(name = "id")
  val articleIds: Set<Long> = emptySet(),

  val name: String,

  val url: String,

  val image: String?,

  @Enumerated(EnumType.STRING)
  val rssStatus: RssStatus,

  val rss: String?,

  val urlCssSelector: String? = null,

  val isShowOnMain: Boolean,

  val createdBy: Long,

  @CreatedDate
  var createdAt: LocalDateTime = LocalDateTime.MIN,
) {

  fun isNew(): Boolean {
    return this.articleIds.isEmpty()
  }

  enum class RssStatus {
    WITH_CONTENT,
    WITHOUT_CONTENT,
    NO_RSS
  }
}