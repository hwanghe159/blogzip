package com.blogzip.api.controller

import com.blogzip.api.dto.BlogCreateRequest
import com.blogzip.api.dto.BlogResponse
import com.blogzip.crawler.service.WebScrapper
import com.blogzip.crawler.service.RssFeedFetcher
import com.blogzip.domain.Blog
import com.blogzip.service.BlogService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate


@RestController
class BlogController(
    private val blogService: BlogService,
    private val rssFeedFetcher: RssFeedFetcher,
    private val webScrapper: WebScrapper,
) {

    @GetMapping("/api/v1/blog/{id}")
    fun get(@PathVariable id: Long): ResponseEntity<BlogResponse> {
        val blog = blogService.findById(id)
        return ResponseEntity.ok(
            BlogResponse(
                id = blog.id!!,
                name = blog.name,
                url = blog.url,
                rss = blog.rss,
                createdBy = blog.createdBy,
                createdAt = blog.createdAt,
            )
        )
    }

    @PostMapping("/api/v1/blog")
    fun save(@RequestBody request: BlogCreateRequest): ResponseEntity<BlogResponse> {
        val url: String = if (request.url.endsWith('/')) {
            request.url.dropLast(1)
        } else {
            request.url
        }
        val name = webScrapper.getTitle(url)
        val rss = webScrapper.convertToRss(url).firstOrNull()
        val blog = blogService.save(
            name,
            url,
            rss,
            rssStatus =
            if (rss == null) Blog.RssStatus.NO_RSS
            else if (rssFeedFetcher.isContentContainsInRss(rss)) Blog.RssStatus.WITH_CONTENT
            else Blog.RssStatus.WITHOUT_CONTENT,
            request.createdBy
        )
        return ResponseEntity.ok(BlogResponse.from(blog))
    }

    @PostMapping("/api/v1/blogs")
    fun saves(@RequestBody request: List<String>): ResponseEntity<List<BlogResponse>> {
        val map = request.map {
            val url: String = if (it.endsWith('/')) {
                it.dropLast(1)
            } else {
                it
            }
            val name = webScrapper.getTitle(url)
            val rss = webScrapper.convertToRss(url)
            blogService.save(
                name = name,
                url = url,
                rss = rss.firstOrNull(),
                rssStatus =
                if (rss.isEmpty()) Blog.RssStatus.NO_RSS
                else if (rssFeedFetcher.isContentContainsInRss(rss.first())) Blog.RssStatus.WITH_CONTENT
                else Blog.RssStatus.WITHOUT_CONTENT,
                createdBy = 1
            )
        }
            .map { BlogResponse.from(it) }
        return ResponseEntity.ok(map)
    }


    @PostMapping("/api/v1/contents")
    fun get(@RequestParam rss: String): ResponseEntity<List<RssFeedFetcher.ArticleData>> {
        val fetchArticles = rssFeedFetcher.fetchContents(rss, LocalDate.of(2024, 3, 21))
        return ResponseEntity.ok(fetchArticles)
    }
}