package com.blogzip.api.controller

import com.blogzip.api.dto.admin.KeywordUpdateRequest
import com.blogzip.service.KeywordService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate


@RestController
class KeywordController(
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
}