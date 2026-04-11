package com.blogzip.domain

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface BlogRepository : JpaRepository<Blog, Long> {

  fun existsByUrl(url: String): Boolean
  fun findByUrl(url: String): Blog?

  @Query(
    """
            select blog
            from Blog blog
            where blog.name like %:query%
             or blog.url like %:query%
    """
  )
  fun search(query: String): List<Blog>

  fun findAllByIsShowOnMain(isShowOnMain: Boolean): List<Blog>

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query(
    """
            update Blog blog
            set blog.name = :name,
                blog.image = :image,
                blog.rss = :rss,
                blog.rssStatus = :rssStatus
            where blog.id = :blogId
    """
  )
  fun updateMetadataById(
    @Param("blogId") blogId: Long,
    @Param("name") name: String,
    @Param("image") image: String?,
    @Param("rss") rss: String?,
    @Param("rssStatus") rssStatus: Blog.RssStatus,
  ): Int
}
