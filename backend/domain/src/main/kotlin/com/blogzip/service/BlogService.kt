package com.blogzip.service

import com.blogzip.common.DomainException
import com.blogzip.common.ErrorCode
import com.blogzip.domain.Blog
import com.blogzip.domain.BlogRepository
import com.blogzip.domain.BlogUrl
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class BlogService(private val repository: BlogRepository) {

  @Transactional(readOnly = true)
  fun findById(id: Long): Blog {
    return repository.findById(id)
      .orElseThrow { DomainException(ErrorCode.BLOG_NOT_FOUND) }
  }

  @Transactional(readOnly = true)
  fun existsByUrl(url: BlogUrl): Boolean {
    return repository.existsByUrl(url.toString())
  }

  @Transactional(readOnly = true)
  fun findByUrl(url: String): Blog {
    val normalizedUrl = url.trim().trimEnd('/')
    return repository.findByUrl(normalizedUrl)
      ?: repository.findByUrl("$normalizedUrl/")
      ?: repository.search(normalizedUrl)
        .firstOrNull { it.url.trim().trimEnd('/') == normalizedUrl }
      ?: throw DomainException(ErrorCode.BLOG_NOT_FOUND)
  }

  @Transactional(readOnly = true)
  fun findAll(): List<Blog> {
    return repository.findAll()
  }

  @Transactional(readOnly = true)
  fun findAllByIds(ids: Collection<Long>): List<Blog> {
    if (ids.isEmpty()) {
      return emptyList()
    }
    return repository.findAllById(ids)
  }

  @Transactional(readOnly = true)
  fun findBlogsRequiringCssSelector(): List<Blog> {
    return repository.findAllRequiringUrlCssSelector(Blog.RssStatus.NO_RSS)
  }

  @Transactional
  fun updateMetadata(
    blogId: Long,
    name: String,
    image: String?,
    rss: String?,
    rssStatus: Blog.RssStatus,
  ) {
    val updatedCount = repository.updateMetadataById(
      blogId = blogId,
      name = name,
      image = image,
      rss = rss,
      rssStatus = rssStatus,
    )
    if (updatedCount == 0) {
      throw DomainException(ErrorCode.BLOG_NOT_FOUND)
    }
  }

  @Transactional
  fun save(
    name: String,
    url: String,
    image: String?,
    rss: String?,
    rssStatus: Blog.RssStatus,
    createdBy: Long
  ): Blog {
    return repository.save(
      Blog(
        name = name,
        url = url,
        image = image,
        rss = rss,
        rssStatus = rssStatus,
        isShowOnMain = false,
        createdBy = createdBy
      )
    )
  }

  @Transactional(readOnly = true)
  fun search(query: String): List<Blog> {
    val trimmedQuery = query.trim()
    val normalizedQuery = trimmedQuery.trimEnd('/')

    if (trimmedQuery.isBlank()) {
      return repository.search(trimmedQuery)
    }
    if (normalizedQuery == trimmedQuery) {
      return repository.search(trimmedQuery)
    }

    return (repository.search(trimmedQuery) + repository.search(normalizedQuery))
      .distinctBy { it.id }
  }

  @Transactional
  fun updateCssSelector(blogId: Long, cssSelector: String?) {
    val blog = findById(blogId)
    blog.updateUrlCssSelector(cssSelector)
  }
}
