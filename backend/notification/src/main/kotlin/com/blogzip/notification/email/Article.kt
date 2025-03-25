package com.blogzip.notification.email

import com.blogzip.notification.email.EmailTemplateParser.Companion.FORMATTER
import java.time.LocalDate

data class Article(
  val title: String,
  val url: String,
  val summary: String,
  val blogName: String,
  val keywords: List<String>,
  val createdDate: LocalDate,
) {
  val createdDateString: String = createdDate.format(FORMATTER)
}