package com.blogzip.api.controller

import com.blogzip.api.auth.Authenticated
import com.blogzip.api.auth.AuthenticatedUser
import com.blogzip.api.dto.BlogCreateRequest
import com.blogzip.api.dto.BlogCreateResponse
import com.blogzip.api.dto.BlogResponse
import com.blogzip.common.DomainException
import com.blogzip.common.ErrorCode
import com.blogzip.crawler.service.CrawlerHttpClient
import com.blogzip.crawler.service.RssFeedFetcher
import com.blogzip.domain.Blog
import com.blogzip.domain.BlogUrl
import com.blogzip.service.BlogService
import com.blogzip.slack.SlackSender
import com.blogzip.slack.SlackSender.SlackChannel.MONITORING
import io.swagger.v3.oas.annotations.Parameter
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.URISyntaxException


@RestController
class BlogController(
  private val blogService: BlogService,
  private val rssFeedFetcher: RssFeedFetcher,
  private val crawlerHttpClient: CrawlerHttpClient,
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
    val blogUrl = try {
      BlogUrl.from(request.url)
    } catch (e: URISyntaxException) {
      throw DomainException(ErrorCode.BLOG_URL_NOT_VALID)
    }
    if (blogService.existsByUrl(blogUrl)) {
      throw DomainException(ErrorCode.BLOG_URL_DUPLICATED)
    }
    val metadata = crawlerHttpClient.getMetadata(blogUrl.toString())
    if (metadata == null) {
      slackSender.sendMessageAsync(
        MONITORING,
        "crawler metadata 조회 실패. url=$blogUrl"
      )
    }
    val image = metadata?.imageUrl
    val rss = metadata?.rss
    if (image == null || rss == null) {
      slackSender.sendMessageAsync(
        MONITORING,
        "imageUrl==null 또는 rss==null. url=$blogUrl, metadata=$metadata"
      )
    }
    // todo rss 가 있어도 cloudflare에 의해 차단되는 경우가 있음. 이 경우엔 NO_RSS 가 되어야 함
    val rssStatus =
      if (rss == null) Blog.RssStatus.NO_RSS
      else if (rssFeedFetcher.isContentContainsInRss(rss)) Blog.RssStatus.WITH_CONTENT
      else Blog.RssStatus.WITHOUT_CONTENT

    if (rssStatus == Blog.RssStatus.NO_RSS) {
      /**
       * 아래는 모든 title 정보를 가져오는 js 코드
       * const articles = document.querySelectorAll('...');
       * const titles = Array.from(articles).map(article => article.textContent.trim())
      */
      slackSender.sendMessageAsync(MONITORING, "url_css_selector 직접 추가 필요. url=$blogUrl")
    }
    val blog = blogService.save(
      name = metadata?.title ?: blogUrl.toString(),
      url = blogUrl.toString(),
      image = image,
      rss = rss,
      rssStatus = rssStatus,
      createdBy = user.id,
    )
    return ResponseEntity.ok(BlogCreateResponse.from(blog))
  }
}
