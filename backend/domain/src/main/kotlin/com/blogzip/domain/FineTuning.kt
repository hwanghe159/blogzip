package com.blogzip.domain

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

@Entity
class FineTuning(

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val id: Long? = null,

  val articleId: Long,

  var summary: String,

  val keywords: String,
) {
  fun update(tunedSummary: String): FineTuning {
    this.summary = tunedSummary
    return this
  }
}