package com.blogzip.api.controller

import com.blogzip.api.auth.Authenticated
import com.blogzip.api.auth.AuthenticatedUser
import com.blogzip.api.dto.BlogCreateRequest
import com.blogzip.api.dto.BlogCreateResponse
import com.blogzip.api.dto.BlogResponse
import com.blogzip.common.DomainException
import com.blogzip.common.ErrorCode
import com.blogzip.crawler.service.RssFeedFetcher
import com.blogzip.crawler.service.ChromeWebScrapper
import com.blogzip.crawler.service.WebScrapper
import com.blogzip.domain.Blog
import com.blogzip.domain.BlogUrl
import com.blogzip.slack.SlackSender
import com.blogzip.slack.SlackSender.SlackChannel.*
import com.blogzip.service.BlogService
import io.swagger.v3.oas.annotations.Parameter
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


@RestController
class BlogController(
    private val blogService: BlogService,
    private val rssFeedFetcher: RssFeedFetcher,
    private val chromeWebScrapper: WebScrapper,
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
    ): ResponseEntity<BlogCreateResponse> {
        val blogUrl = BlogUrl.from(request.url)
        if (blogService.existsByUrl(blogUrl)) {
            throw DomainException(ErrorCode.BLOG_URL_DUPLICATED)
        }
        val metadata = chromeWebScrapper.getMetadata(blogUrl.toString())
        // todo rss 가 있어도 cloudflare에 의해 차단되는 경우가 있음. 이 경우엔 NO_RSS 가 되어야 함
        val rssStatus =
            if (metadata.rss == null) Blog.RssStatus.NO_RSS
            else if (rssFeedFetcher.isContentContainsInRss(metadata.rss!!)) Blog.RssStatus.WITH_CONTENT
            else Blog.RssStatus.WITHOUT_CONTENT

        if (rssStatus == Blog.RssStatus.NO_RSS) {
            slackSender.sendMessageAsync(MONITORING, "url_css_selector 직접 추가 필요. url=$blogUrl")
        }
        val blog = blogService.save(
            name = metadata.title,
            url = blogUrl.toString(),
            image = metadata.imageUrl,
            rss = metadata.rss,
            rssStatus = rssStatus,
            createdBy = user.id,
        )
        return ResponseEntity.ok(BlogCreateResponse.from(blog))
    }
}