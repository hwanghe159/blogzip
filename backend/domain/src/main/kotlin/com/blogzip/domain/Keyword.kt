package com.blogzip.domain

import com.blogzip.common.DomainException
import com.blogzip.common.ErrorCode
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

  var value: String,

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "head_id")
  var head: Keyword? = null,

  var isVisible: Boolean = true,

  @OneToMany(fetch = FetchType.LAZY, cascade = [CascadeType.ALL], mappedBy = "head")
  val followers: List<Keyword> = mutableListOf(),

  @CreatedDate
  var createdAt: LocalDateTime = LocalDateTime.MIN,
) {

  fun isHead(): Boolean {
    return this.head == null
  }

  fun follow(keyword: Keyword): Keyword {
    this.head = keyword
    return this
  }

  fun updateValue(value: String): Keyword {
    this.value = value
    return this
  }

  fun updateVisible(visible: Boolean) {
    this.isVisible = visible
  }

  fun mergeInto(destination: Keyword) {
    if (!isHead() || !destination.isHead()) {
      throw DomainException(ErrorCode.KEYWORD_UPDATE_FAILED)
    }
    for (follower in this.followers) {
      follower.follow(destination)
    }
    follow(destination)
  }
}