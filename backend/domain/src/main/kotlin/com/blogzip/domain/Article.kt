package com.blogzip.domain

import jakarta.persistence.*
import java.time.LocalDate

@Entity
class Article(

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val id: Long? = null,

  @Column(name = "blog_id")
  val blogId: Long,

  val title: String,

  val content: String,

  val url: String,

  var createdDate: LocalDate? = null,

  @Column(name = "is_visible")
  var isVisible: Boolean = true,
) {

  @PrePersist
  fun prePersist() {
    if (createdDate == null) {
      createdDate = LocalDate.now()
    }
  }

  override fun toString(): String {
    return "Article(id=$id, blogId=$blogId, title='$title', content='$content', url='$url', createdDate=$createdDate, isVisible=$isVisible)"
  }
}
