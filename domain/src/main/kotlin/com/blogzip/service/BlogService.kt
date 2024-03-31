package com.blogzip.service

import com.blogzip.domain.Blog
import com.blogzip.domain.BlogRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class BlogService(private val repository: BlogRepository) {

    @Transactional(readOnly = true)
    fun findById(id: Long): Blog {
        return repository.findById(id)
            .orElseThrow { RuntimeException() }
    }

    @Transactional(readOnly = true)
    fun findAll(): List<Blog> {
        return repository.findAll()
    }

    @Transactional
    fun save(
        name: String,
        url: String,
        rss: String?,
        rssStatus: Blog.RssStatus,
        createdBy: Long
    ): Blog {
        return repository.save(
            Blog(
                name = name,
                url = url,
                rss = rss,
                rssStatus = rssStatus,
                createdBy = createdBy
            )
        )
    }
}