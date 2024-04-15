package com.blogzip.api.controller

import com.blogzip.api.auth.Authenticated
import com.blogzip.api.auth.AuthenticatedUser
import com.blogzip.api.dto.BlogCreateRequest
import com.blogzip.api.dto.BlogResponse
import com.blogzip.common.DomainException
import com.blogzip.common.ErrorCode
import com.blogzip.crawler.service.RssFeedFetcher
import com.blogzip.crawler.service.WebScrapper
import com.blogzip.domain.Blog
import com.blogzip.service.BlogService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


@RestController
class BlogController(
    private val blogService: BlogService,
    private val rssFeedFetcher: RssFeedFetcher,
    private val webScrapper: WebScrapper,
) {

    // todo EC2에선 swagger 예제만 나오는중..
    @GetMapping("/api/v1/blog/{id}")
    fun get(@PathVariable id: Long): ResponseEntity<BlogResponse> {
        val blog = blogService.findById(id)
        return ResponseEntity.ok(
            BlogResponse(
                id = blog.id!!,
                name = blog.name,
                url = blog.url,
                rss = blog.rss,
                rssStatus = blog.rssStatus,
                createdBy = blog.createdBy,
                createdAt = blog.createdAt,
            )
        )
    }

    @PostMapping("/api/v1/blog")
    fun save(
        @Authenticated user: AuthenticatedUser,
        @RequestBody request: BlogCreateRequest
    ): ResponseEntity<BlogResponse> {
        val url: String = if (request.url.endsWith('/')) {
            request.url.dropLast(1)
        } else {
            request.url
        }
        if (blogService.existsByUrl(url)) {
            throw DomainException(ErrorCode.BLOG_URL_DUPLICATED)
        }

        val name = webScrapper.getTitle(url) // todo 실패시 처리
        val rss = webScrapper.convertToRss(url).firstOrNull()
        val blog = blogService.save(
            name,
            url,
            rss,
            rssStatus =
            if (rss == null) Blog.RssStatus.NO_RSS
            else if (rssFeedFetcher.isContentContainsInRss(rss)) Blog.RssStatus.WITH_CONTENT
            else Blog.RssStatus.WITHOUT_CONTENT,
            user.id,
        )
        return ResponseEntity.ok(BlogResponse.from(blog))
    }
}