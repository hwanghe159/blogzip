package com.blogzip.domain

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface FineTuningRepository : JpaRepository<FineTuning, Long> {

    @Query(
        """
            select fineTuning
            from FineTuning fineTuning
            where fineTuning.articleId = :articleId
    """
    )
    fun findByArticleId(articleId: Long): FineTuning?
}