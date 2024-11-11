package com.blogzip.ai

import com.blogzip.ai.config.FeignConfig
import feign.Param
import feign.form.FormData
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestPart

@FeignClient(
    name = "OpenAIApiClient",
    url = "https://api.openai.com",
    configuration = [FeignConfig::class]
)
interface OpenAIApiClient {

    // 파일 업로드 API
    @PostMapping("/v1/files", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun uploadFile(
        @RequestPart("file") file: FormData,
        @RequestPart("purpose") purpose: String,
    ): Map<String, Any>

    // Batch 생성 API
    @PostMapping("/v1/completions", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun createBatch(
        request: BatchCreateRequest
    ): Map<String, Any>

    // Batch 상태 조회 API
    @GetMapping("/v1/batches/{batchId}", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun getBatchStatus(
        @PathVariable batchId: String,
    ): Map<String, Any>

    // Batch 결과 조회 API
    @GetMapping("/v1/files/{fileId}/content", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun getBatchResult(
        @Param("fileId") fileId: String,
    ): Map<String, Any>

//    data class FileUploadRequest(
//        @FormDataPart("purpose") val purpose: String,
//        @FormDataPart("file") val file: File,
//    )

    data class BatchCreateRequest(
        // todo case 확인
        val inputFileId: String,
        val endpoint: String,
        val completionWindow: String,
    )
}