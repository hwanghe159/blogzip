package com.blogzip.ai.summary

import com.blogzip.ai.common.JsonlConverter
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component

@Component
class ArticleContentBatchSummarizer(
    private val openAiApiClient: OpenAiApiClient,
    private val jsonlConverter: JsonlConverter,
    private val objectMapper: ObjectMapper,
    private val singleRequestGenerator: SingleRequestGenerator,
) {
    fun summarizeAndGetKeywordsAll(articles: List<Article>): List<SummarizeAndKeywordsResult> {
        if (articles.isEmpty()) {
            return emptyList()
        }
        // jsonl 파일 생성
        val requests = articles.map {
            singleRequestGenerator.generate(
                customId = it.id.toString(),
                content = it.content,
                model = "ft:gpt-4o-mini-2024-07-18:personal::AT34qLAv"
            )
        }
        val jsonl = jsonlConverter.objectsToJsonl(requests)

        // jsonl 파일 업로드
        val fileId = openAiApiClient.uploadFile(
            file = CustomMultipartFile(
                input = jsonl.toByteArray(),
                name = "file",
                originalFileName = "batch_api_request.jsonl",
                contentType = null
            ),
            purpose = "batch"
        )["id"] as String

        // batch 생성
        val batchId = openAiApiClient.createBatch(
            OpenAiApiClient.BatchCreateRequest(
                input_file_id = fileId,
                endpoint = "/v1/chat/completions",
                completion_window = "24h"
            )
        )["id"] as String

        // batch 상태 체크
        var outputFileId: String? = null
        while (true) {
            val response = openAiApiClient.getBatchStatus(batchId)
            val status = response["status"] as String
            if (status == "completed") {
                outputFileId = response["output_file_id"] as String
                break
            } else if (status == "failed") {
                break
            }
            Thread.sleep(10000)
        }

        // batch 결과 조회
        val resultJsonl = openAiApiClient.getBatchResult(outputFileId!!)
        return jsonlConverter.jsonlToObjects(
            resultJsonl.toString(Charsets.UTF_8),
            BatchResponse::class.java
        )
            .map {
                val summaryAndKeywordsJson =
                    objectMapper.readTree(it.response?.body?.choices?.first()?.message?.content)
                val summary = summaryAndKeywordsJson["summary"].textValue()
                val keywords = summaryAndKeywordsJson["keywords"].map { it.textValue() }
                SummarizeAndKeywordsResult(
                    id = it.customId?.toLong()!!,
                    summary = summary,
                    summarizedBy = it.response?.body?.model!!,
                    keywords = keywords
                )
            }
    }

    data class Article(
        val id: Long,
        val content: String,
    )

    data class SummarizeAndKeywordsResult(
        val id: Long,
        val summary: String,
        val keywords: List<String>,
        val summarizedBy: String,
    )
}