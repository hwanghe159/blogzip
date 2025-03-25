package com.blogzip.api.controller

import com.blogzip.ai.common.JsonlConverter
import com.blogzip.ai.tuning.FineTuningDataset
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
  private val jsonlConverter: JsonlConverter,
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
    val fineTuningAndArticle =
      fineTuningService.update(id, request.tunedSummary, request.keywords)
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

  /**
   * {"messages": [
   *   {"role": "system", "content": "한국인을 대상으로 하는 테크블로그 내용 요약기 및 키워드 추출기"},
   *   {"role": "user", "content": "...블로그 글 내용..."},
   *   {"role": "assistant", "content": "{\"summary\": \"요약된 내용\", \"keywords\": [\"키워드1\", \"키워드2\", \"키워드3\"]}"}
   * ]}
   */
  private fun convertToJsonl(tunings: List<FineTuningAndArticle>): String {
    val objects: List<FineTuningDataset> = tunings
      .map {
        FineTuningDataset(
          messages = listOf(
            FineTuningDataset.Message(
              role = "system",
              content = "한국인을 대상으로 하는 테크블로그 내용 요약기 및 키워드 추출기"
            ),
            FineTuningDataset.Message(
              role = "user",
              content = it.article.content
            ),
            FineTuningDataset.Message(
              role = "assistant",
              content = jsonlObjectMapper.writeValueAsString(
                SummaryAndKeywords(
                  summary = it.fineTuning.summary,
                  keywords = it.fineTuning.keywords.split(",").map { it.trim() },
                )
              )
            ),
          )
        )
      }
    return jsonlConverter.objectsToJsonl(objects)
  }

  data class SummaryAndKeywords(
    val summary: String,
    val keywords: List<String>,
  )
}