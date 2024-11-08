package com.blogzip.domain

import org.springframework.data.jpa.repository.JpaRepository

interface KeywordRepository : JpaRepository<Keyword, Long> {

    fun findAllByValueIn(values: Collection<String>): List<Keyword>
}