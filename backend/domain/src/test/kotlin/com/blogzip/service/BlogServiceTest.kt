package com.blogzip.service

import com.blogzip.domain.Blog
import com.blogzip.domain.BlogRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoSettings
import java.util.Optional

@MockitoSettings
class BlogServiceTest {

  @Mock
  lateinit var blogRepository: BlogRepository

  @InjectMocks
  lateinit var blogService: BlogService

  @DisplayName("블로그 css selector를 수정할 수 있다.")
  @Test
  fun updateCssSelector() {
    val blog = Blog(
      id = 1,
      name = "blogzip",
      url = "https://blogzip.test",
      image = null,
      rssStatus = Blog.RssStatus.NO_RSS,
      rss = null,
      urlCssSelector = ".before a",
      isShowOnMain = false,
      createdBy = 1,
    )
    `when`(blogRepository.findById(1L))
      .thenReturn(Optional.of(blog))

    blogService.updateCssSelector(1L, ".post-list a")

    assertEquals(".post-list a", blog.urlCssSelector)
  }
}
