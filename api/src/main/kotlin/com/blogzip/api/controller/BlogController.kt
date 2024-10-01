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
import com.blogzip.notification.common.SlackSender
import com.blogzip.notification.common.SlackSender.SlackChannel.*
import com.blogzip.service.BlogService
import io.swagger.v3.oas.annotations.Parameter
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.lang.Exception


@RestController
class BlogController(
    private val blogService: BlogService,
    private val rssFeedFetcher: RssFeedFetcher,
    private val webScrapper: WebScrapper,
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

    // todo 부가정보 비동기로 update 하는 것 고려
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

        val blogTitle = webScrapper.getTitle(url)
        val imageUrl = try {
            webScrapper.getImageUrl(url)
        } catch (e: Exception) {
            slackSender.sendStackTraceAsync(ERROR_LOG, e)
            null
        }
        val rss = webScrapper.convertToRss(url).firstOrNull()
        // todo rss 가 있어도 cloudflare에 의해 차단되는 경우가 있음. 이 경우엔 NO_RSS 가 되어야 함
        val rssStatus =
            if (rss == null) Blog.RssStatus.NO_RSS
            else if (rssFeedFetcher.isContentContainsInRss(rss)) Blog.RssStatus.WITH_CONTENT
            else Blog.RssStatus.WITHOUT_CONTENT

        if (rssStatus == Blog.RssStatus.NO_RSS) {
            slackSender.sendMessageAsync(MONITORING, "url_css_selector 직접 추가 필요. url=$url")
        }
        val blog = blogService.save(
            name = blogTitle,
            url = url,
            image = imageUrl,
            rss = rss,
            rssStatus = rssStatus,
            createdBy = user.id,
        )
        return ResponseEntity.ok(BlogResponse.from(blog))
    }
}