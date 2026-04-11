package com.blogzip.domain

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(
  indexes = [
    Index(name = "idx_article_summary_article_id", columnList = "article_id"),
    Index(name = "idx_article_summary_article_id_is_applied", columnList = "article_id,is_applied"),
  ]
)
class ArticleSummary(

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val id: Long? = null,

  val articleId: Long,

  @Column(length = 2000)
  val summary: String,

  @Column(length = 100)
  val summarizedBy: String,

  var isApplied: Boolean = false,

  @CreatedDate
  var createdAt: LocalDateTime = LocalDateTime.MIN,

  @LastModifiedDate
  var updatedAt: LocalDateTime = LocalDateTime.MIN,
) {
  fun apply() {
    this.isApplied = true
  }
}
