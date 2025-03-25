package com.blogzip.domain

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@EntityListeners(AuditingEntityListener::class)
class ArticleKeyword(

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val id: Long? = null,

  val articleId: Long,

  var headKeywordId: Long,

  @CreatedDate
  var createdAt: LocalDateTime = LocalDateTime.MIN,

  @LastModifiedDate
  var updatedAt: LocalDateTime = LocalDateTime.MIN,
) {
  fun changeHeadKeywordId(headKeywordId: Long): ArticleKeyword {
    this.headKeywordId = headKeywordId
    return this
  }
}