package com.blogzip.api.controller

import com.blogzip.api.auth.Authenticated
import com.blogzip.api.auth.AuthenticatedUser
import com.blogzip.api.dto.*
import com.blogzip.service.KeywordService
import com.blogzip.service.ReadLaterService
import io.swagger.v3.oas.annotations.Parameter
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


@RestController
class ReadLaterController(
    private val readLaterService: ReadLaterService,
    private val keywordService: KeywordService,
) {

    @GetMapping("/api/v1/read-later")
    fun getReadLater(@Parameter(hidden = true) @Authenticated user: AuthenticatedUser)
            : ResponseEntity<List<ReadLaterWithKeywordsResponse>> {
        val readLaters = readLaterService.findAllByUserId(user.id)
        val articleIds = readLaters.map { it.article.id!! }
        val keywords = keywordService.getAllByArticleIds(articleIds)
        val response = readLaters.map {
            ReadLaterWithKeywordsResponse.of(
                it.readLater,
                it.article,
                keywords = keywords[it.article.id]
                    ?.filter { it.isVisible }
                    ?.map { it.value }
                    ?: emptyList())
        }
        return ResponseEntity.ok(response)
    }

    // todo
    @GetMapping("/api/v1/read-later/search")
    fun search(
        @Parameter(hidden = true) @Authenticated user: AuthenticatedUser,
        @RequestParam(required = false) next: Long?,
        @RequestParam(required = false, defaultValue = "20") size: Int,
    ): ResponseEntity<PaginationResponse<ArticleResponse>> {
        val response = readLaterService.search(user.id, next, size)
        return ResponseEntity.ok(PaginationResponse(emptyList(), response.next))
    }

    @PostMapping("/api/v1/read-later")
    fun addReadLater(
        @Parameter(hidden = true) @Authenticated user: AuthenticatedUser,
        @RequestBody request: ReadLaterCreateRequest,
    ): ResponseEntity<ReadLaterResponse> {
        val readLaterAndArticle = readLaterService.save(user.id, request.articleId)
        return ResponseEntity.ok(
            ReadLaterResponse.of(
                readLaterAndArticle.readLater,
                readLaterAndArticle.article
            )
        )
    }

    @DeleteMapping("/api/v1/read-later")
    fun deleteReadLater(
        @Parameter(hidden = true) @Authenticated user: AuthenticatedUser,
        @RequestBody request: ReadLaterDeleteRequest,
    ): ResponseEntity<Void> {
        readLaterService.delete(user.id, request.articleId)
        return ResponseEntity.noContent().build()
    }
}