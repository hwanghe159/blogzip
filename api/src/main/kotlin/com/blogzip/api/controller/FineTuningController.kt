package com.blogzip.api.controller

import com.blogzip.api.dto.FineTuningRequest
import com.blogzip.api.dto.TunedArticleResponse
import com.blogzip.dto.FineTuningAndArticle
import com.blogzip.service.ArticleQueryService
import com.blogzip.service.FineTuningService
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.core.io.InputStreamResource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.io.ByteArrayInputStream


@RestController
class FineTuningController(
    private val articleQueryService: ArticleQueryService,
    private val fineTuningService: FineTuningService,
    private val jsonlObjectMapper: ObjectMapper,
) {

    @GetMapping("/api/v1/article/{id}/fine-tuning")
    fun getFineTuning(@PathVariable id: Long): ResponseEntity<TunedArticleResponse> {
        val article = articleQueryService.findById(id)
        val fineTuning = fineTuningService.findByArticleId(id)
            ?: return ResponseEntity.ok(TunedArticleResponse.from(article))
        return ResponseEntity.ok(TunedArticleResponse.of(fineTuning, article))
    }

    @GetMapping("/api/v1/fine-tuning")
    fun getAll(): ResponseEntity<List<TunedArticleResponse>> {
        val response = fineTuningService.findAll()
            .map { TunedArticleResponse.of(it.fineTuning, it.article) }
        return ResponseEntity.ok(response)
    }

    @PostMapping("/api/v1/article/{id}/fine-tuning")
    fun updateFineTuning(
        @PathVariable id: Long,
        @RequestBody request: FineTuningRequest,
    ): ResponseEntity<TunedArticleResponse> {
        val fineTuningAndArticle = fineTuningService.update(id, request.tunedSummary)
        return ResponseEntity.ok(
            TunedArticleResponse.of(
                fineTuningAndArticle.fineTuning,
                fineTuningAndArticle.article
            )
        )
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

    private fun convertToJsonl(tunings: List<FineTuningAndArticle>): String {
        val sb = StringBuilder()
        tunings.forEach {
//            val map = mapOf(
//                "prompt" to it.article.content,
//                "completion" to it.summary
//            )

            val map = mapOf(
                "messages" to listOf(
                    mapOf(
                        "role" to "system",
                        "content" to "너는 한국인을 대상으로 하는 테크블로그 내용 요약기야. markdown 형식의 글을 입력받으면 내용을 요약하는 역할이야. 내용을 5줄 정도의 줄글로 요약해줘. 말투는 친근하지만 정중한 존댓말인 '~~요'체를 써줘. 바로 요약 내용만 말해줘."
                    ),
                    mapOf("role" to "user", "content" to it.article.content),
                    mapOf("role" to "assistant", "content" to it.fineTuning.summary),
                )
            )
            sb.appendLine(jsonlObjectMapper.writeValueAsString(map))
        }
        return sb.toString()
    }
}