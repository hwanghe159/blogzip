package com.blogzip.api.controller

import com.blogzip.api.auth.Authenticated
import com.blogzip.api.auth.AuthenticatedUser
import com.blogzip.api.dto.ArticleResponse
import com.blogzip.api.dto.PaginationResponse
import com.blogzip.api.dto.admin.ArticleKeywordsAddRequest
import com.blogzip.service.ArticleQueryService
import com.blogzip.service.KeywordService
import io.swagger.v3.oas.annotations.Parameter
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate


@RestController
class ArticleController(
  private val articleQueryService: ArticleQueryService,
  private val keywordService: KeywordService,
) {

  @GetMapping("/api/v1/article")
  fun get(
    @RequestParam(required = true) from: LocalDate,
    @RequestParam(required = false) to: LocalDate?,
    @RequestParam(required = false) next: Long?,
    @RequestParam(required = false, defaultValue = "20") size: Int,
  ): ResponseEntity<PaginationResponse<ArticleResponse>> {
    val searchedArticles = articleQueryService.search(from, to, next, size)
    val articleIds = searchedArticles.articles.map { it.id }
    val keywords = keywordService.getAllByArticleIds(articleIds)
    return ResponseEntity.ok(
      PaginationResponse(
        items = searchedArticles.articles.map {
          ArticleResponse.from(
            article = it,
            keywords = keywords[it.id]
              ?.filter { it.isVisible }
              ?.map { it.value }
              ?: emptyList()
          )
        },
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
    val searchedArticles = articleQueryService.searchMy(from, to, next, size, user.id)
    val articleIds = searchedArticles.articles.map { it.id }
    val keywords = keywordService.getAllByArticleIds(articleIds)
    return ResponseEntity.ok(
      PaginationResponse(
        items = searchedArticles.articles.map {
          ArticleResponse.from(
            article = it,
            keywords = keywords[it.id]
              ?.filter { it.isVisible }
              ?.map { it.value }
              ?: emptyList()
          )
        },
        next = searchedArticles.next
      )
    )
  }

  @PostMapping("/api/admin/article/{articleId}/keyword")
  fun addKeywords(
    @PathVariable articleId: Long,
    @RequestBody request: ArticleKeywordsAddRequest,
  ): ResponseEntity<com.blogzip.api.dto.admin.ArticleResponse> {
    val article = articleQueryService.findById(articleId)
    keywordService.addArticleKeywords(articleId, request.values)
    val keywords = keywordService.getKeywordDetails(articleId)
    return ResponseEntity.ok(
      com.blogzip.api.dto.admin.ArticleResponse.from(article, keywords)
    )
  }
}