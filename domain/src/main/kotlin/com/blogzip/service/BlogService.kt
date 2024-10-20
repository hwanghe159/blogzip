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
    fun findAll(): List<Blog> {
        return repository.findAll()
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
        return repository.search(query)
    }

    @Transactional(readOnly = true)
    fun getSubscribedBlogs(userId: Long): List<Blog> {
        return repository.getSubscribedBlogs(userId)
    }
}