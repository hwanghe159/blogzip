package com.blogzip.api.controller

import com.blogzip.api.auth.Authenticated
import com.blogzip.api.auth.AuthenticatedUser
import com.blogzip.api.dto.BlogCreateRequest
import com.blogzip.api.dto.BlogCreateResponse
import com.blogzip.api.dto.BlogResponse
import com.blogzip.crawler.service.SeleniumBlogMetadataScrapper
import com.blogzip.crawler.service.RssFeedFetcher
import com.blogzip.service.BlogCreateService
import com.blogzip.slack.SlackSender
import com.blogzip.service.BlogService
import io.swagger.v3.oas.annotations.Parameter
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


@RestController
class BlogController(
    private val blogCreateService: BlogCreateService,
    private val blogService: BlogService,
    private val rssFeedFetcher: RssFeedFetcher,
    private val blogMetadataScrapper: SeleniumBlogMetadataScrapper,
    private val slackSender: SlackSender,
) {

    @GetMapping("/api/v1/blog/{id}")
    fun get(@PathVariable id: Long): ResponseEntity<BlogResponse> {
        val blog = blogService.findById(id)
        return ResponseEntity.ok(BlogResponse.from(blog))
    }

    @GetMapping("/api/v1/blog")
    fun getAll(): ResponseEntity<List<BlogResponse>> {
        val response = blogService.findAll()
            .map { BlogResponse.from(it) }
        return ResponseEntity.ok(response)
    }

    @GetMapping("/api/v1/blog/search")
    fun searchBlog(
        @RequestParam(required = true) query: String,
    ): ResponseEntity<List<BlogResponse>> {
        val response = blogService.search(query)
            .map { BlogResponse.from(it) }
        return ResponseEntity.ok(response)
    }

    @PostMapping("/api/v1/blog")
    fun save(
        @Parameter(hidden = true) @Authenticated user: AuthenticatedUser,
        @RequestBody request: BlogCreateRequest
    ): ResponseEntity<BlogCreateResponse> {
        val result = blogCreateService.create(request.url, user.id)
        return ResponseEntity.ok(BlogCreateResponse.from(result))
    }
}