package com.blogzip.domain

import org.springframework.data.jpa.repository.JpaRepository

interface KeywordRepository : JpaRepository<Keyword, Long> {

    fun existsByValue(value: String): Boolean
    fun findByValue(value: String): Keyword?
    fun findAllByValueIn(values: Collection<String>): List<Keyword>
}