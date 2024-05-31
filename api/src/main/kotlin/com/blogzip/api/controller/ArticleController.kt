package com.blogzip.api.controller

import com.blogzip.api.auth.Authenticated
import com.blogzip.api.auth.AuthenticatedUser
import com.blogzip.api.dto.ArticleResponse
import com.blogzip.api.dto.PaginationResponse
import com.blogzip.service.ArticleService
import io.swagger.v3.oas.annotations.Parameter
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate


@RestController
class ArticleController(
    private val articleService: ArticleService,
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
        val searchedArticles = articleService.searchMy(user.id, from, to, next, size)
        return ResponseEntity.ok(
            PaginationResponse(
                items = searchedArticles.articles.map { ArticleResponse.from(it) },
                next = searchedArticles.next
            )
        )
    }
}