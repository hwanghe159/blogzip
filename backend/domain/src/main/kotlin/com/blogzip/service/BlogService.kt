package com.blogzip.service

import com.blogzip.common.DomainException
import com.blogzip.common.ErrorCode
import com.blogzip.domain.Blog
import com.blogzip.domain.BlogRepository
import com.blogzip.domain.BlogUrl
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.net.URISyntaxException

data class AdminSearchedBlogs(
  val items: List<Blog>,
  val next: Long?,
)

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

  @Transactional(readOnly = true)
  fun searchForAdmin(next: Long?, size: Int, query: String?): AdminSearchedBlogs {
    val normalizedQuery = query?.trim().orEmpty()
    val blogs = repository.searchForAdmin(
      next = next,
      query = normalizedQuery,
      pageable = PageRequest.of(0, size + 1),
    )
    if (blogs.isEmpty()) {
      return AdminSearchedBlogs(items = emptyList(), next = null)
    }
    val existsNext = blogs.size == size + 1
    val finalBlogs = blogs.take(size)
    return AdminSearchedBlogs(
      items = finalBlogs,
      next = if (existsNext) finalBlogs.last().id else null,
    )
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

  @Transactional
  fun updateForAdmin(
    blogId: Long,
    name: String,
    url: String,
    image: String?,
    rss: String?,
    urlCssSelector: String?,
    rssStatus: Blog.RssStatus,
    isShowOnMain: Boolean,
  ): Blog {
    val normalizedUrl = try {
      BlogUrl.from(url).toString()
    } catch (exception: URISyntaxException) {
      throw DomainException(ErrorCode.BLOG_URL_NOT_VALID)
    }
    if (repository.existsByUrlAndIdNot(normalizedUrl, blogId)) {
      throw DomainException(ErrorCode.BLOG_URL_DUPLICATED)
    }
    val updatedCount = repository.updateAdminFieldsById(
      blogId = blogId,
      name = name.trim(),
      url = normalizedUrl,
      image = image?.trim()?.ifBlank { null },
      rss = rss?.trim()?.ifBlank { null },
      urlCssSelector = urlCssSelector?.trim()?.ifBlank { null },
      rssStatus = rssStatus,
      isShowOnMain = isShowOnMain,
    )
    if (updatedCount == 0) {
      throw DomainException(ErrorCode.BLOG_NOT_FOUND)
    }
    return findById(blogId)
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  fun updateMetadataPerBlog(
    blogId: Long,
    name: String,
    image: String?,
    rss: String?,
    rssStatus: Blog.RssStatus,
    shouldUpdateCssSelector: Boolean,
    urlCssSelector: String?,
  ) {
    updateMetadata(
      blogId = blogId,
      name = name,
      image = image,
      rss = rss,
      rssStatus = rssStatus,
    )
    if (shouldUpdateCssSelector) {
      updateCssSelector(blogId, urlCssSelector)
    }
  }
}
