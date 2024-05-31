package com.blogzip.domain

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface BlogRepository : JpaRepository<Blog, Long> {

    fun existsByUrl(url: String): Boolean

    @Query(
        """
            select blog
            from Blog blog
            where blog.name like %:query%
             or blog.url like %:query%
    """
    )
    fun search(query: String): List<Blog>
}