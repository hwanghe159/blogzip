package com.blogzip.domain

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface KeywordRepository : JpaRepository<Keyword, Long> {

  fun existsByValue(value: String): Boolean
  fun findByValue(value: String): Keyword?
  fun findAllByValueIn(values: Collection<String>): List<Keyword>

  @Query(
    """
    select keyword
    from Keyword keyword
    left join fetch keyword.head
    """
  )
  fun findAllWithHead(): List<Keyword>
}
