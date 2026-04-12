package com.blogzip.api.controller

import com.blogzip.api.auth.Authenticated
import com.blogzip.api.auth.AuthenticatedUser
import com.blogzip.api.admin.AdminRequired
import com.blogzip.api.dto.ArticleResponse
import com.blogzip.api.dto.ArticleReportRequest
import com.blogzip.api.dto.ArticleReportResponse
import com.blogzip.api.dto.PaginationResponse
import com.blogzip.api.dto.admin.ArticleKeywordsAddRequest
import com.blogzip.dto.SearchedArticles
import com.blogzip.service.ArticleCommandService
import com.blogzip.service.ArticleQueryService
import com.blogzip.service.KeywordService
import com.blogzip.service.ArticleReportService
import io.swagger.v3.oas.annotations.Parameter
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate


@RestController
class ArticleController(
  private val articleQueryService: ArticleQueryService,
  private val keywordService: KeywordService,
  private val articleReportService: ArticleReportService,
  private val articleCommandService: ArticleCommandService,
) {

  @GetMapping("/api/v1/article")
  fun get(
    @RequestParam(required = true) from: LocalDate,
    @RequestParam(required = false) to: LocalDate?,
    @RequestParam(required = false) keyword: String?,
    @RequestParam(required = false) keywords: List<String>?,
    @RequestParam(required = false) next: Long?,
    @RequestParam(required = false, defaultValue = "20") size: Int,
  ): ResponseEntity<PaginationResponse<ArticleResponse>> {
    val resolvedKeywords = resolveKeywords(keyword = keyword, keywords = keywords)
    val searchedArticles =
      if (resolvedKeywords.isEmpty()) {
        articleQueryService.search(from, to, next, size)
      } else {
        articleQueryService.searchByKeywordValues(resolvedKeywords, next, size)
      }
    return ResponseEntity.ok(
      buildArticlePaginationResponse(searchedArticles)
    )
  }

  @GetMapping("/api/v1/my/article")
  fun getMyArticles(
    @Parameter(hidden = true) @Authenticated user: AuthenticatedUser,
    @RequestParam(required = true) from: LocalDate,
    @RequestParam(required = false) to: LocalDate?,
    @RequestParam(required = false) keyword: String?,
    @RequestParam(required = false) keywords: List<String>?,
    @RequestParam(required = false) next: Long?,
    @RequestParam(required = false, defaultValue = "20") size: Int,
  ): ResponseEntity<PaginationResponse<ArticleResponse>> {
    val resolvedKeywords = resolveKeywords(keyword = keyword, keywords = keywords)
    val searchedArticles =
      if (resolvedKeywords.isEmpty()) {
        articleQueryService.searchMy(from, to, next, size, user.id)
      } else {
        articleQueryService.searchMyByKeywordValues(
          keywordValues = resolvedKeywords,
          next = next,
          size = size,
          userId = user.id,
        )
      }
    return ResponseEntity.ok(
      buildArticlePaginationResponse(searchedArticles)
    )
  }

  @GetMapping("/api/v1/article/keyword/{keywordId}")
  fun getByKeywordId(
    @PathVariable keywordId: Long,
    @RequestParam(required = false) next: Long?,
    @RequestParam(required = false, defaultValue = "20") size: Int,
  ): ResponseEntity<PaginationResponse<ArticleResponse>> {
    return ResponseEntity.ok(
      buildArticlePaginationResponse(articleQueryService.searchByKeywordId(keywordId, next, size))
    )
  }

  @GetMapping("/api/v1/my/article/keyword/{keywordId}")
  fun getMyArticlesByKeywordId(
    @Parameter(hidden = true) @Authenticated user: AuthenticatedUser,
    @PathVariable keywordId: Long,
    @RequestParam(required = false) next: Long?,
    @RequestParam(required = false, defaultValue = "20") size: Int,
  ): ResponseEntity<PaginationResponse<ArticleResponse>> {
    return ResponseEntity.ok(
      buildArticlePaginationResponse(
        articleQueryService.searchMyByKeywordId(
          keywordId = keywordId,
          next = next,
          size = size,
          userId = user.id,
        )
      )
    )
  }

  @AdminRequired
  @PostMapping("/api/admin/article/{articleId}/keyword")
  fun addKeywords(
    @PathVariable articleId: Long,
    @RequestBody request: ArticleKeywordsAddRequest,
  ): ResponseEntity<com.blogzip.api.dto.admin.ArticleResponse> {
    val article = articleQueryService.findById(articleId)
    keywordService.addArticleKeywords(articleId, request.values)
    val keywords = keywordService.getKeywordDetails(articleId)
    val appliedSummary = articleCommandService.getAppliedSummary(articleId)
    return ResponseEntity.ok(
      com.blogzip.api.dto.admin.ArticleResponse.from(
        article = article,
        headKeywords = keywords,
        summary = appliedSummary?.summary,
        summarizedBy = appliedSummary?.summarizedBy,
      )
    )
  }

  @PostMapping("/api/v1/article/{articleId}/report")
  fun reportArticle(
    @Parameter(hidden = true) @Authenticated user: AuthenticatedUser,
    @PathVariable articleId: Long,
    @Valid @RequestBody request: ArticleReportRequest,
  ): ResponseEntity<ArticleReportResponse> {
    val report = articleReportService.report(
      userId = user.id,
      articleId = articleId,
      reason = request.reason,
      detail = request.detail,
    )
    return ResponseEntity.ok(ArticleReportResponse.from(report))
  }

  private fun buildArticlePaginationResponse(
    searchedArticles: SearchedArticles,
  ): PaginationResponse<ArticleResponse> {
    val articleIds = searchedArticles.articles.map { it.id }
    val keywords = keywordService.getAllByArticleIds(articleIds)
    return PaginationResponse(
      items = searchedArticles.articles.map {
        ArticleResponse.from(
          article = it,
          keywords = keywords[it.id]
            ?.filter { keyword -> keyword.isVisible }
            ?.map { keyword -> keyword.value }
            ?: emptyList()
        )
      },
      next = searchedArticles.next
    )
  }

  private fun resolveKeywords(
    keyword: String?,
    keywords: List<String>?,
  ): List<String> {
    return buildList {
      if (!keyword.isNullOrBlank()) {
        add(keyword)
      }
      if (!keywords.isNullOrEmpty()) {
        addAll(keywords)
      }
    }
      .map { it.trim() }
      .filter { it.isNotBlank() }
      .distinct()
  }
}
