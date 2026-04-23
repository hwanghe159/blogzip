package com.blogzip.domain

import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface BlogRepository : JpaRepository<Blog, Long> {

  fun existsByUrl(url: String): Boolean
  fun existsByUrlAndIdNot(url: String, id: Long): Boolean
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

  @Query(
    """
            select blog
            from Blog blog
            where (:query = '' 
                or lower(blog.name) like lower(concat('%', :query, '%'))
                or lower(blog.url) like lower(concat('%', :query, '%')))
              and (:next is null or blog.id < :next)
            order by blog.id desc
    """
  )
  fun searchForAdmin(
    @Param("next") next: Long?,
    @Param("query") query: String,
    pageable: Pageable,
  ): List<Blog>

  @Query(
    """
            select blog
            from Blog blog
            where blog.rssStatus = :rssStatus
              and (blog.urlCssSelector is null or length(trim(blog.urlCssSelector)) = 0)
            order by blog.createdAt desc
    """
  )
  fun findAllRequiringUrlCssSelector(@Param("rssStatus") rssStatus: Blog.RssStatus): List<Blog>

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

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query(
    """
            update Blog blog
            set blog.name = :name,
                blog.url = :url,
                blog.image = :image,
                blog.rss = :rss,
                blog.urlCssSelector = :urlCssSelector,
                blog.rssStatus = :rssStatus,
                blog.isShowOnMain = :isShowOnMain
            where blog.id = :blogId
    """
  )
  fun updateAdminFieldsById(
    @Param("blogId") blogId: Long,
    @Param("name") name: String,
    @Param("url") url: String,
    @Param("image") image: String?,
    @Param("rss") rss: String?,
    @Param("urlCssSelector") urlCssSelector: String?,
    @Param("rssStatus") rssStatus: Blog.RssStatus,
    @Param("isShowOnMain") isShowOnMain: Boolean,
  ): Int
}
