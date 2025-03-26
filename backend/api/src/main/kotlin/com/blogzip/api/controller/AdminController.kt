package com.blogzip.api.controller

import com.blogzip.ai.common.JsonlConverter
import com.blogzip.ai.summary.BatchResponse
import com.blogzip.ai.summary.OpenAiApiClient
import com.blogzip.ai.summary.SummarizedArticle
import com.blogzip.ai.summary.SummarizedArticleResult
import com.blogzip.api.admin.AdminTokenRequired
import com.blogzip.api.dto.admin.KeywordUpdateRequest
import com.blogzip.service.ArticleCommandService
import com.blogzip.service.KeywordService
import com.blogzip.slack.SlackSender
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
class AdminController(
  private val keywordService: KeywordService,
  private val openAiApiClient: OpenAiApiClient,
  private val jsonlConverter: JsonlConverter,
  private val objectMapper: ObjectMapper,
  private val articleCommandService: ArticleCommandService,
  private val slackSender: SlackSender,
) {

  @AdminTokenRequired
  @PatchMapping("/api/admin/keyword/{value}")
  fun updateKeyword(
    @PathVariable value: String,
    @RequestBody request: KeywordUpdateRequest,
  ) {
    keywordService.update(value, request.value, request.isVisible)
  }

  @AdminTokenRequired
  @PostMapping("/api/admin/keyword/merge")
  fun mergeKeywords(
    @RequestParam(required = true) src: String,
    @RequestParam(required = true) dest: String,
  ) {
    keywordService.merge(src, dest)
  }

  @AdminTokenRequired
  @PostMapping("/api/admin/openai/batches/{batchId}/re-run")
  fun reRunOpenAiBatches(
    @PathVariable batchId: String,
  ): ResponseEntity<Boolean> {
    val response = openAiApiClient.getBatch(batchId)
    val status = response["status"] as String
    if (status != "completed") {
      return ResponseEntity.ok(false)
    }
    val outputFileId = response["output_file_id"] as String
    val resultJsonl = openAiApiClient.getFileContent(outputFileId)
    val summarizedArticle: List<SummarizedArticleResult> = jsonlConverter.jsonlToObjects(
      resultJsonl.toString(Charsets.UTF_8),
      BatchResponse::class.java
    )
      .map {
        val summaryAndKeywordsJson =
          objectMapper.readTree(it.response?.body?.choices?.first()?.message?.content)
        val summary = summaryAndKeywordsJson["summary"].textValue()
        val keywords = summaryAndKeywordsJson["keywords"].map { it.textValue() }
        SummarizedArticleResult.Success(
          SummarizedArticle(
            id = it.customId?.toLong()!!,
            summary = summary,
            summarizedBy = it.response?.body?.model!!,
            keywords = keywords
          )
        )
      }
    summarizedArticle.forEach {
      it.handle(
        onSuccess = { result ->
          articleCommandService.updateSummary(result.id, result.summary, result.summarizedBy)
          keywordService.addArticleKeywords(result.id, result.keywords)
        },
        onFailure = { articleId, throwable ->
          val exception = RuntimeException("$articleId 요약 실패", throwable)
          slackSender.sendStackTraceAsync(SlackSender.SlackChannel.ERROR_LOG, exception)
        }
      )
    }
    return ResponseEntity.ok(true)
  }
}