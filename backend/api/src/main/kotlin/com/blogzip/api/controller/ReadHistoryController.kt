package com.blogzip.api.controller

import com.blogzip.api.auth.Authenticated
import com.blogzip.api.auth.AuthenticatedUser
import com.blogzip.api.dto.ReadHistoryCreateRequest
import com.blogzip.service.ReadHistoryService
import io.swagger.v3.oas.annotations.Parameter
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class ReadHistoryController(
  private val readHistoryService: ReadHistoryService,
) {

  // userId를 활용한 어뷰징을 막기 위해 성공/실패여부 및 body 미응답
  @PostMapping("/api/v1/user/{userId}/read")
  fun addHistory(
    @PathVariable userId: Long,
    @RequestBody request: ReadHistoryCreateRequest,
  ): ResponseEntity<Void> {
    try {
      readHistoryService.save(userId, request.articleUrl)
    } catch (_: Exception) {
    }
    return ResponseEntity.ok().build()
  }

  @PostMapping("/api/v1/article/{articleId}/read")
  fun addHistory(
    @Parameter(hidden = true) @Authenticated user: AuthenticatedUser,
    @PathVariable articleId: Long,
  ): ResponseEntity<Void> {
    readHistoryService.save(user.id, articleId)
    return ResponseEntity.ok().build()
  }
}