package com.blogzip.api.controller

import com.blogzip.api.auth.Authenticated
import com.blogzip.api.auth.AuthenticatedUser
import com.blogzip.api.dto.*
import com.blogzip.service.ReadLaterService
import io.swagger.v3.oas.annotations.Parameter
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


@RestController
class ReadLaterController(
    private val readLaterService: ReadLaterService,
) {

    @GetMapping("/api/v1/read-later")
    fun getReadLater(@Parameter(hidden = true) @Authenticated user: AuthenticatedUser)
            : ResponseEntity<List<ReadLaterResponse>> {
        val response = readLaterService.findAllByUserId(user.id)
            .map { ReadLaterResponse.of(it.readLater, it.article) }
        return ResponseEntity.ok(response)
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