package com.blogzip.domain

import org.springframework.data.jpa.repository.JpaRepository

interface BlogRepository : JpaRepository<Blog, Long> {

    fun existsByUrl(url: String): Boolean
}