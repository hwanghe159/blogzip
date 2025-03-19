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
import org.springframework.stereotype.Component

@Component
class ArticleContentSequentialSummarizer(
  private val openAiProperties: OpenAiProperties,
  private val objectMapper: ObjectMapper,
) : ArticleContentSummarizer {

  override fun summarizeAndGetKeywordsAll(articles: List<ArticleToSummarize>): List<SummarizedArticleResult> {
    return articles.map { summarizeAndGetKeywords(it) }
  }

  private fun summarizeAndGetKeywords(article: ArticleToSummarize): SummarizedArticleResult {
    val summarizeResult = summarize(article.content)
    return if (summarizeResult != null) {
      SummarizedArticleResult(
        result = SummarizedArticleResult.Result.SUCCESS,
        article = SummarizedArticle(
          id = article.id,
          summary = summarizeResult.summary,
          keywords = summarizeResult.keywords,
          summarizedBy = summarizeResult.summarizedBy,
        )
      )
    } else {
      SummarizedArticleResult(
        result = SummarizedArticleResult.Result.FAIL,
        article = null
      )
    }
  }

  @OptIn(BetaOpenAI::class)
  private fun summarize(content: String): SummarizeResult? = runBlocking {
    val openAI = OpenAI(openAiProperties.apiKey)
    val threadId = openAI.thread().id
    val assistantId = AssistantId(openAiProperties.assistantId)

    try {
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
      do {
        delay(3000)
        // GET https://api.openai.com/v1/threads/{thread_id}/runs/{run_id}
        // https://platform.openai.com/docs/api-reference/runs-v1/getRun
        val retrievedRun = openAI.getRun(threadId = threadId, runId = run.id)
      } while (retrievedRun.status != Status.Completed)
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
    } catch (e: Exception) {
      return@runBlocking null
    }
  }

  data class SummarizeResult(
    val summary: String,
    val keywords: List<String>,
    val summarizedBy: String,
  )
}