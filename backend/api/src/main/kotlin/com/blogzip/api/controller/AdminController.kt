package com.blogzip.api.controller

import com.blogzip.api.dto.admin.KeywordUpdateRequest
import com.blogzip.service.KeywordService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

// todo admin token 사용
@RestController
class AdminController(
    private val keywordService: KeywordService,
) {

    @PatchMapping("/api/admin/keyword/{value}")
    fun updateKeyword(
        @PathVariable value: String,
        @RequestBody request: KeywordUpdateRequest,
    ) {
        keywordService.update(value, request.value, request.isVisible)
    }

    @PostMapping("/api/admin/keyword/merge")
    fun mergeKeywords(
        @RequestParam(required = true) src: String,
        @RequestParam(required = true) dest: String,
    ) {
        keywordService.merge(src, dest)
    }

    // todo
    @PostMapping("/api/admin/openai/batches/{batchId}/re-run")
    fun reRunOpenAiBatches(
        @PathVariable batchId: String,
    ): ResponseEntity<Boolean> {
        return ResponseEntity.ok(true)
    }
}