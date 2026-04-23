package com.blogzip.api.dto.admin

import com.blogzip.domain.Blog
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotBlank

data class AdminBlogUpdateRequest(
  @field:NotBlank(message = "블로그 이름을 입력해 주세요.")
  val name: String,
  @field:NotBlank(message = "블로그 URL을 입력해 주세요.")
  val url: String,
  val image: String? = null,
  val rss: String? = null,
  val urlCssSelector: String? = null,
  val rssStatus: Blog.RssStatus,
  @param:JsonProperty("isShowOnMain")
  @get:JsonProperty("isShowOnMain")
  val isShowOnMain: Boolean = false,
)
