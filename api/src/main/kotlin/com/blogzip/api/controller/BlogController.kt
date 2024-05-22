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
import io.swagger.v3.oas.annotations.Parameter
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
        return ResponseEntity.ok(BlogResponse.from(blog))
    }

    @PostMapping("/api/v1/blog")
    fun save(
        @Parameter(hidden = true) @Authenticated user: AuthenticatedUser,
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
        val rssStatus =
            if (rss == null) Blog.RssStatus.NO_RSS
            else if (rssFeedFetcher.isContentContainsInRss(rss)) Blog.RssStatus.WITH_CONTENT
            else Blog.RssStatus.WITHOUT_CONTENT
        val blog = blogService.save(
            name,
            url,
            image = null, // todo 이미지 조회
            rss,
            rssStatus = rssStatus,
            user.id,
        )
        return ResponseEntity.ok(BlogResponse.from(blog))
    }
}