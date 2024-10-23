package com.blogzip.api.controller

import com.blogzip.api.auth.Authenticated
import com.blogzip.api.auth.AuthenticatedUser
import com.blogzip.api.dto.ArticleResponse
import com.blogzip.api.dto.PaginationResponse
import com.blogzip.service.ArticleService
import com.blogzip.service.BlogService
import io.swagger.v3.oas.annotations.Parameter
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate


@RestController
class ArticleController(
    private val articleService: ArticleService,
    private val blogService: BlogService,
) {

    @GetMapping("/api/v1/article")
    fun get(
        @RequestParam(required = true) from: LocalDate,
        @RequestParam(required = false) to: LocalDate?,
        @RequestParam(required = false) next: Long?,
        @RequestParam(required = false, defaultValue = "20") size: Int,
    ): ResponseEntity<PaginationResponse<ArticleResponse>> {
        val searchedArticles = articleService.search(from, to, next, size)
        return ResponseEntity.ok(
            PaginationResponse(
                items = searchedArticles.articles.map { ArticleResponse.from(it) },
                next = searchedArticles.next
            )
        )
    }

    @GetMapping("/api/v1/my/article")
    fun getMyArticles(
        @Parameter(hidden = true) @Authenticated user: AuthenticatedUser,
        @RequestParam(required = true) from: LocalDate,
        @RequestParam(required = false) to: LocalDate?,
        @RequestParam(required = false) next: Long?,
        @RequestParam(required = false, defaultValue = "20") size: Int,
    ): ResponseEntity<PaginationResponse<ArticleResponse>> {
        // fetch join과 페이지네이션을 같이 사용하면 데이터를 전부 가져와 메모리에서 거른다.
        // 이를 방지하기 위해 2개의 쿼리로 나눔.
        val blogs = blogService.getSubscribedBlogs(user.id)
        val searchedArticles = articleService.searchMy(blogs, from, to, next, size, user.id)
        return ResponseEntity.ok(
            PaginationResponse(
                items = searchedArticles.articles.map { ArticleResponse.from(it) },
                next = searchedArticles.next
            )
        )
    }
}