package com.blogzip.domain

import org.springframework.data.jpa.repository.JpaRepository

interface ReadHistoryRepository : JpaRepository<ReadHistory, Long> {
}