package com.blogzip.ai.summary

import com.aallam.openai.api.BetaOpenAI
import com.aallam.openai.api.assistant.AssistantId
import com.aallam.openai.api.core.Role
import com.aallam.openai.api.core.Status
import com.aallam.openai.api.message.MessageContent
import com.aallam.openai.api.message.MessageRequest
import com.aallam.openai.api.run.RunRequest
import com.aallam.openai.client.OpenAI
import com.blogzip.ai.config.OpenAiProperties
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.concurrent.TimeoutException

@Component
class ArticleContentSequentialSummarizer(
  private val openAiProperties: OpenAiProperties,
  private val objectMapper: ObjectMapper,
) : ArticleContentSummarizer {

  private val log = LoggerFactory.getLogger(ArticleContentSequentialSummarizer::class.java)

  companion object {
    private const val RUN_POLL_INTERVAL_MS = 3_000L
    private const val RUN_TIMEOUT_MS = 5 * 60 * 1_000L
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

  @OptIn(BetaOpenAI::class)
  private fun summarize(content: String): SummarizeResult = runBlocking {
    val openAI = OpenAI(openAiProperties.apiKey)
    val threadId = openAI.thread().id
    val assistantId = AssistantId(openAiProperties.assistantId)

    // POST https://api.openai.com/v1/threads/{thread_id}/messages
    // https://platform.openai.com/docs/api-reference/messages-v1/createMessage
    openAI.message(
      threadId = threadId,
      request = MessageRequest(
        role = Role.User,
        content = content
      )
    )
    // POST https://api.openai.com/v1/threads/{thread_id}/runs
    // https://platform.openai.com/docs/api-reference/runs-v1/createRun
    val run = openAI.createRun(
      threadId,
      request = RunRequest(
        assistantId = assistantId
      )
    )

    val startedAt = System.currentTimeMillis()
    var pollCount = 0
    while (true) {
      delay(RUN_POLL_INTERVAL_MS)
      pollCount++

      // GET https://api.openai.com/v1/threads/{thread_id}/runs/{run_id}
      // https://platform.openai.com/docs/api-reference/runs-v1/getRun
      val retrievedRun = openAI.getRun(threadId = threadId, runId = run.id)
      val elapsedMs = System.currentTimeMillis() - startedAt
      val status = retrievedRun.status

      if (status == Status.Completed) {
        break
      }

      if (status == Status.RequiresAction) {
        val reason = buildString {
          append("OpenAI run requires action. ")
          append("runId=${run.id}, ")
          append("status=$status, ")
          append("requiredAction=${retrievedRun.requiredAction}, ")
          append("lastError=${retrievedRun.lastError}")
        }
        log.error(reason)
        throw IllegalStateException(reason)
      }

      if (status == Status.Failed || status == Status.Cancelled || status == Status.Expired) {
        val reason = buildString {
          append("OpenAI run terminated without completion. ")
          append("runId=${run.id}, ")
          append("status=$status, ")
          append("lastError=${retrievedRun.lastError}")
        }
        log.error(reason)
        throw IllegalStateException(reason)
      }

      if (elapsedMs >= RUN_TIMEOUT_MS) {
        val reason = buildString {
          append("OpenAI run polling timed out. ")
          append("runId=${run.id}, ")
          append("status=$status, ")
          append("pollCount=$pollCount, ")
          append("elapsedMs=$elapsedMs, ")
          append("requiredAction=${retrievedRun.requiredAction}, ")
          append("lastError=${retrievedRun.lastError}")
        }
        log.error(reason)
        throw TimeoutException(reason)
      }

      if (pollCount % 10 == 0) {
        log.info(
          "OpenAI run is still in progress. runId=${run.id}, status=$status, pollCount=$pollCount, elapsedMs=$elapsedMs"
        )
      }
    }

    // GET https://api.openai.com/v1/threads/{thread_id}/messages
    // https://platform.openai.com/docs/api-reference/messages-v1/listMessages
    val messages = openAI.messages(threadId)
    val textContent = messages.first().content.first() as MessageContent.Text
    val jsonResponse = objectMapper.readTree(textContent.text.value)
    return@runBlocking SummarizeResult(
      summary = jsonResponse.get("summary").asText(),
      keywords = jsonResponse.get("keywords").map { it.asText() },
      summarizedBy = run.model.id
    )
  }

  data class SummarizeResult(
    val summary: String,
    val keywords: List<String>,
    val summarizedBy: String,
  )
}
