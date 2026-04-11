package com.blogzip.domain

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(
  uniqueConstraints = [
    UniqueConstraint(
      name = "udx_article_report_user_article",
      columnNames = ["article_id", "user_id"]
    )
  ],
  indexes = [
    Index(name = "idx_article_report_article_id", columnList = "article_id"),
    Index(name = "idx_article_report_user_id", columnList = "user_id"),
  ]
)
class ArticleReport(

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val id: Long? = null,

  val articleId: Long,

  val userId: Long,

  var reason: String,

  var detail: String? = null,

  @Enumerated(EnumType.STRING)
  var status: Status = Status.RECEIVED,

  @CreatedDate
  var createdAt: LocalDateTime = LocalDateTime.MIN,

  @LastModifiedDate
  var updatedAt: LocalDateTime = LocalDateTime.MIN,
) {

  fun update(reason: String, detail: String?) {
    this.reason = reason
    this.detail = detail
  }

  enum class Status {
    RECEIVED,
    REVIEWED,
    RESOLVED,
    DISMISSED,
  }
}
