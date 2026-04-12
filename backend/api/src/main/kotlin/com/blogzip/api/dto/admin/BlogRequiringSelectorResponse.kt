package com.blogzip.api.dto.admin

import com.blogzip.domain.Blog
import java.time.LocalDateTime

data class BlogRequiringSelectorResponse(
  val id: Long,
  val name: String,
  val url: String,
  val rssStatus: Blog.RssStatus,
  val createdAt: LocalDateTime,
) {
  companion object {
    fun from(blog: Blog): BlogRequiringSelectorResponse {
      return BlogRequiringSelectorResponse(
        id = blog.id!!,
        name = blog.name,
        url = blog.url,
        rssStatus = blog.rssStatus,
        createdAt = blog.createdAt,
      )
    }
  }
}
