package com.blogzip.api.controller

import com.blogzip.api.dto.BlogCreateRequest
import com.blogzip.api.dto.BlogResponse
import com.blogzip.crawler.service.ArticleCrawler
import com.blogzip.crawler.service.ArticleFetcher
import com.blogzip.service.BlogService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.view.feed.AbstractAtomFeedView
import java.time.LocalDate


@RestController
class BlogController(
    private val blogService: BlogService,
    private val articleFetcher: ArticleFetcher,
    private val articleCrawler: ArticleCrawler,
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
        val name = articleCrawler.getTitle(url)
        val rss = articleCrawler.convertToRss(url)
        val blog = blogService.save(name, url, rss.first(), request.createdBy)
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
            val name = articleCrawler.getTitle(url)
            val rss = articleCrawler.convertToRss(url)
            blogService.save(name, url, if(rss.isEmpty()) null else rss.first(), 1)
        }
            .map { BlogResponse.from(it) }
        return ResponseEntity.ok(map)
    }


    @PostMapping("/api/v1/contents")
    fun get(@RequestParam rss: String): ResponseEntity<List<ArticleFetcher.ArticleData>> {
        val fetchArticles = articleFetcher.fetchArticles(rss, LocalDate.of(2024, 3, 21))
        return ResponseEntity.ok(fetchArticles)
    }
}