package com.blogzip.batch.summarize

import com.blogzip.ai.BatchResponse
import com.blogzip.ai.JsonlConverter
import com.blogzip.ai.OpenAiApiClient
import com.blogzip.ai.SingleRequestGenerator
import com.blogzip.slack.SlackSender
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.core.io.ByteArrayResource
import org.springframework.stereotype.Component

@Component
class ArticleContentBatchSummarizer(
    private val openAiApiClient: OpenAiApiClient,
    private val jsonlConverter: JsonlConverter,
    private val objectMapper: ObjectMapper,
    private val singleRequestGenerator: SingleRequestGenerator,
    private val slackSender: SlackSender,
) {
    fun summarizeAndGetKeywordsAll(articles: List<Article>): List<SummarizeAndKeywordsResult> {
        // jsonl 파일 생성
        // {"custom_id": "article.id", "method": "POST", "url": "/v1/chat/completions", "body": {"model": "gpt-3.5-turbo-0125", "messages": [{"role": "system", "content": "You are a helpful assistant."},{"role": "user", "content": "Hello world!"}],"max_tokens": 1000}}
        val requests = articles.map {
            singleRequestGenerator.generate(customId = it.id.toString(), content = it.content)
        }
        val jsonl = jsonlConverter.objectsToJsonl(requests)

        // jsonl 파일 업로드
        // 요청
        // curl https://api.openai.com/v1/files \
        //  -H "Authorization: Bearer $OPENAI_API_KEY" \
        //  -F purpose="batch" \
        //  -F file="@batchinput.jsonl"
        // 응답
        // {
        //    "object": "file",
        //    "id": "file-jhh5IimAcxtK90m89jRWtEkc",
        //    "purpose": "batch",
        //    "filename": "batch_api_request.jsonl",
        //    "bytes": 40320,
        //    "created_at": 1731319210,
        //    "status": "processed",
        //    "status_details": null
        //}
        val fileId = openAiApiClient.uploadFile(
            file = ByteArrayResource(jsonl.toByteArray()),
            purpose = "batch"
        )["id"] as String

        // batch 생성
        // 요청
        // curl https://api.openai.com/v1/batches \
        //  -H "Authorization: Bearer $OPENAI_API_KEY" \
        //  -H "Content-Type: application/json" \
        //  -d '{
        //    "input_file_id": "file-abc123",
        //    "endpoint": "/v1/chat/completions",
        //    "completion_window": "24h"
        //  }'
        // 응답
        // {
        //    "id": "batch_6731d7772df48190ac34492d8ce579d6",
        //    "object": "batch",
        //    "endpoint": "/v1/chat/completions",
        //    "errors": null,
        //    "input_file_id": "file-jhh5IimAcxtK90m89jRWtEkc",
        //    "completion_window": "24h",
        //    "status": "validating",
        //    "output_file_id": null,
        //    "error_file_id": null,
        //    "created_at": 1731319671,
        //    "in_progress_at": null,
        //    "expires_at": 1731406071,
        //    "finalizing_at": null,
        //    "completed_at": null,
        //    "failed_at": null,
        //    "expired_at": null,
        //    "cancelling_at": null,
        //    "cancelled_at": null,
        //    "request_counts": {
        //        "total": 0,
        //        "completed": 0,
        //        "failed": 0
        //    },
        //    "metadata": null
        //}
        val batchId = openAiApiClient.createBatch(
            OpenAiApiClient.BatchCreateRequest(
                inputFileId = fileId,
                endpoint = "/v1/chat/completions",
                completionWindow = "24h"
            )
        )["id"] as String

        // batch 상태 체크
        // curl https://api.openai.com/v1/batches/batch_abc123 \
        //  -H "Authorization: Bearer $OPENAI_API_KEY" \
        //  -H "Content-Type: application/json" \
        // 응답
        // {
        //    "id": "batch_6731d7772df48190ac34492d8ce579d6",
        //    "object": "batch",
        //    "endpoint": "/v1/chat/completions",
        //    "errors": null,
        //    "input_file_id": "file-jhh5IimAcxtK90m89jRWtEkc",
        //    "completion_window": "24h",
        //    "status": "completed",
        //    "output_file_id": null,
        //    "error_file_id": "file-E7B9GIy4BdFpkTsCq7bIf8Az",
        //    "created_at": 1731319671,
        //    "in_progress_at": 1731319671,
        //    "expires_at": 1731406071,
        //    "finalizing_at": 1731319673,
        //    "completed_at": 1731319673,
        //    "failed_at": null,
        //    "expired_at": null,
        //    "cancelling_at": null,
        //    "cancelled_at": null,
        //    "request_counts": {
        //        "total": 2,
        //        "completed": 0,
        //        "failed": 2
        //    },
        //    "metadata": null
        //}

        var outputFileId: String? = null
        while (true) {
            val response = openAiApiClient.getBatchStatus(batchId)
            if (response["status"] as String == "completed") {
                outputFileId = response["output_file_id"] as String
                break
            }
            Thread.sleep(5000)
        }

        // batch 결과 조회
        // curl https://api.openai.com/v1/files/file-xyz123/content \
        //  -H "Authorization: Bearer $OPENAI_API_KEY"
        // 응답
        val resultJsonl = openAiApiClient.getBatchResult(outputFileId!!)
        return jsonlConverter.jsonlToObjects(resultJsonl.toString(), BatchResponse::class.java)
            .map {
                val summaryAndKeywordsJson = objectMapper.readTree(it.response?.body?.choices?.first()?.message?.content)
                val summary = summaryAndKeywordsJson["summary"].toString()
                val keywords = summaryAndKeywordsJson["keywords"].map { it.toString() }
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