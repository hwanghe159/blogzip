package com.blogzip.ai.summary

import com.blogzip.ai.config.FeignConfig
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.multipart.MultipartFile

@FeignClient(
  name = "openAI",
  url = "https://api.openai.com",
  configuration = [FeignConfig::class]
)
interface OpenAiApiClient {

  // 파일 업로드 API
  @PostMapping("/v1/files", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
  fun uploadFile(
    @RequestPart("file") file: MultipartFile,
    @RequestPart("purpose") purpose: String,
  ): Map<String, Any>

  // Batch 생성 API
  @PostMapping("/v1/batches", consumes = [MediaType.APPLICATION_JSON_VALUE])
  fun createBatch(
    request: BatchCreateRequest
  ): Map<String, Any>

  // Batch 상태 조회 API
  @GetMapping("/v1/batches/{batchId}", consumes = [MediaType.APPLICATION_JSON_VALUE])
  fun getBatch(
    @PathVariable batchId: String,
  ): Map<String, Any>

  // Batch 결과 조회 API
  @GetMapping("/v1/files/{fileId}/content")
  fun getFileContent(
    @PathVariable fileId: String,
  ): ByteArray

  @GetMapping("/v1/files/{fileId}", consumes = [MediaType.APPLICATION_JSON_VALUE])
  fun getFile(
    @PathVariable fileId: String,
  ): Map<String, Any>

  // todo 설정으로 snake_case 적용
  data class BatchCreateRequest(
    val input_file_id: String,
    val endpoint: String,
    val completion_window: String,
  )
}