package com.blogzip.api.controller

import com.blogzip.api.dto.FineTuningRequest
import com.blogzip.api.dto.TunedArticleResponse
import com.blogzip.domain.FineTuning
import com.blogzip.service.ArticleService
import com.blogzip.service.FineTuningService
import org.springframework.core.io.InputStreamResource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.io.ByteArrayInputStream


@RestController
class FineTuningController(
    private val articleService: ArticleService,
    private val fineTuningService: FineTuningService,
) {

    @GetMapping("/api/v1/article/{id}/fine-tuning")
    fun getFineTuning(@PathVariable id: Long): ResponseEntity<TunedArticleResponse> {
        val article = articleService.findById(id)
        val fineTuning = fineTuningService.findByArticleId(id)
            ?: return ResponseEntity.ok(TunedArticleResponse.from(article))
        return ResponseEntity.ok(TunedArticleResponse.from(fineTuning))
    }

    @GetMapping("/api/v1/fine-tuning")
    fun getAll(): ResponseEntity<List<TunedArticleResponse>> {
        val response = fineTuningService.findAll()
            .map { TunedArticleResponse.from(it) }
        return ResponseEntity.ok(response)
    }

    @PostMapping("/api/v1/article/{id}/fine-tuning")
    fun updateFineTuning(
        @PathVariable id: Long,
        @RequestBody request: FineTuningRequest,
    ): ResponseEntity<TunedArticleResponse> {
        val article = articleService.findById(id)
        val fineTuning = fineTuningService.update(article, request.tunedSummary)
        return ResponseEntity.ok(TunedArticleResponse.from(fineTuning))
    }

    @GetMapping("/api/v1/fine-tuning/download")
    fun download(): ResponseEntity<InputStreamResource> {
        val jsonl = convertToJsonl(fineTuningService.findAll())
        val headers = HttpHeaders()
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=fine-tuning.jsonl")
        return ResponseEntity(
            InputStreamResource(ByteArrayInputStream(jsonl.toByteArray())),
            headers,
            HttpStatus.OK
        )
    }

    private fun convertToJsonl(tunings: List<FineTuning>): String {
        val sb = StringBuilder()
        tunings.filter {
            it.article.summary != null
        }
            .forEach {
                sb.append(
                    """
                        "prompt": "${it.article.summary}","completion": "${it.summary}"
                    """.trimIndent()
                )
            }
        return sb.toString()
    }
}