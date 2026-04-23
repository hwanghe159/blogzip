package com.blogzip.api.dto.admin

import com.blogzip.domain.Blog
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

data class AdminBlogResponse(
  val id: Long,
  val name: String,
  val url: String,
  val image: String?,
  val rss: String?,
  val urlCssSelector: String?,
  val rssStatus: Blog.RssStatus,
  @get:JsonProperty("isShowOnMain")
  val isShowOnMain: Boolean,
  val createdBy: Long,
  val createdAt: LocalDateTime,
) {
  companion object {
    fun from(blog: Blog): AdminBlogResponse {
      return AdminBlogResponse(
        id = blog.id!!,
        name = blog.name,
        url = blog.url,
        image = blog.image,
        rss = blog.rss,
        urlCssSelector = blog.urlCssSelector,
        rssStatus = blog.rssStatus,
        isShowOnMain = blog.isShowOnMain,
        createdBy = blog.createdBy,
        createdAt = blog.createdAt,
      )
    }
  }
}
