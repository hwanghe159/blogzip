package com.blogzip.ai.summary

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class ArticleContentSequentialSummarizer(
  private val openAiApiClient: OpenAiApiClient,
  private val objectMapper: ObjectMapper,
) : ArticleContentSummarizer {

  private val log = LoggerFactory.getLogger(ArticleContentSequentialSummarizer::class.java)

  companion object {
    private const val SUMMARY_MODEL = "ft:gpt-4o-mini-2024-07-18:personal::AT34qLAv"
    private const val FORMAT_TYPE_JSON_SCHEMA = "json_schema"
    private const val SUMMARY_SCHEMA_NAME = "summary_and_keywords"
    private const val SYSTEM_PROMPT = "한국인을 대상으로 하는 테크블로그 내용 요약기 및 키워드 추출기"
    private const val SUMMARY_DESCRIPTION =
      "당신은 마크다운 형식의 텍스트를 5~8줄 정도의 간결한 요약문으로 변환하는 전문가입니다. " +
        "요약 시 다음 지침을 따르세요:\n\n" +
        "1. 친근하면서도 정중한 '~~요'체의 존댓말을 사용하세요.\n" +
        "2. 요약 내용만을 직접적으로 제시하세요.\n" +
        "3. 반말이나 명사로 끝나는 문장은 피하세요.\n" +
        "4. 인사말, 자기소개, 블로그 소개 등은 생략하세요.\n" +
        "5. \"네\", \"알겠어요\", \"요약해드릴게요\" 등의 불필요한 표현은 사용하지 마세요.\n" +
        "6. 대화형 AI가 아닌 요약 전문가로서의 역할에 충실하세요.\n" +
        "7. 응답은 마크다운 형식이 아닌 줄글로 응답하고, 절대 8줄을 넘기지 마세요.\n\n" +
        "입력된 마크다운 텍스트의 핵심 내용을 정확하고 간결하게 전달하는 데 집중하세요. " +
        "요약문은 독자가 원문의 주요 내용을 빠르게 파악할 수 있도록 작성되어야 합니다."
    private const val KEYWORDS_DESCRIPTION =
      "게시물과 관련된 키워드 목록입니다. 이 글의 분야, 대주제, 기술명 등이 될 수 있습니다. " +
        "예를 들어 '백엔드','프론트엔드','AI','DevOps' 같이 분야가 될 수도 있고, " +
        "'소프트스킬','자기개발' 등 주제가 될 수도 있습니다. 또는 'MySQL','Redis','Kafka','Spring' 같은 기술명이 될 수도 있습니다."
  }

  override fun summarizeAndGetKeywordsAll(articles: List<ArticleToSummarize>): List<SummarizedArticleResult> {
    return articles.map { summarizeAndGetKeywords(it) }
  }

  private fun summarizeAndGetKeywords(article: ArticleToSummarize): SummarizedArticleResult {
    try {
      val summarizeResult = summarize(article.content)
      return SummarizedArticleResult.Success(
        article = SummarizedArticle(
          id = article.id,
          summary = summarizeResult.summary,
          keywords = summarizeResult.keywords,
          summarizedBy = summarizeResult.summarizedBy,
        )
      )
    } catch (e: Exception) {
      return SummarizedArticleResult.Failure(article.id, e)
    }
  }

  private fun summarize(content: String): SummarizeResult {
    val response = openAiApiClient.createResponse(buildRequest(content))
    val status = response.status
    if (!status.isNullOrBlank() && status != "completed") {
      val reason = buildString {
        append("OpenAI responses 호출이 완료 상태가 아닙니다. ")
        append("status=$status, ")
        append("errorCode=${response.error?.code}, ")
        append("errorMessage=${response.error?.message}, ")
        append("incompleteDetails=${response.incompleteDetails}")
      }
      log.error(reason)
      throw IllegalStateException(reason)
    }

    val outputText = response.outputText
      ?.takeIf { it.isNotBlank() }
      ?: response.output
        .asSequence()
        .flatMap { it.content.asSequence() }
        .firstOrNull { it.type == "output_text" }
        ?.text
        ?.takeIf { it.isNotBlank() }
      ?: throw IllegalStateException("OpenAI responses output_text가 비어 있습니다.")

    val jsonResponse = objectMapper.readTree(outputText)
    return SummarizeResult(
      summary = jsonResponse.get("summary").asText(),
      keywords = jsonResponse.get("keywords").map { it.asText() },
      summarizedBy = response.model ?: SUMMARY_MODEL
    )
  }

  private fun buildRequest(content: String): OpenAiApiClient.ResponseCreateRequest {
    val schema: Map<String, Any> = mapOf(
      "type" to "object",
      "properties" to mapOf(
        "summary" to mapOf(
          "type" to "string",
          "description" to SUMMARY_DESCRIPTION,
        ),
        "keywords" to mapOf(
          "type" to "array",
          "description" to KEYWORDS_DESCRIPTION,
          "items" to mapOf(
            "type" to "string",
          ),
        ),
      ),
      "required" to listOf("summary", "keywords"),
      "additionalProperties" to false,
    )
    return OpenAiApiClient.ResponseCreateRequest(
      model = SUMMARY_MODEL,
      instructions = SYSTEM_PROMPT,
      input = content,
      text = OpenAiApiClient.ResponseCreateRequest.Text(
        format = OpenAiApiClient.ResponseCreateRequest.Text.Format(
          type = FORMAT_TYPE_JSON_SCHEMA,
          name = SUMMARY_SCHEMA_NAME,
          strict = true,
          schema = schema,
        )
      )
    )
  }

  data class SummarizeResult(
    val summary: String,
    val keywords: List<String>,
    val summarizedBy: String,
  )
}
