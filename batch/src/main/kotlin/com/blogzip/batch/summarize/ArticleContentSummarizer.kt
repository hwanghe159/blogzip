package com.blogzip.batch.summarize

import com.aallam.openai.api.BetaOpenAI
import com.aallam.openai.api.assistant.AssistantId
import com.aallam.openai.api.core.Role
import com.aallam.openai.api.core.Status
import com.aallam.openai.api.message.MessageContent
import com.aallam.openai.api.message.MessageRequest
import com.aallam.openai.api.run.RunRequest
import com.aallam.openai.api.thread.ThreadId
import com.aallam.openai.client.OpenAI
import com.blogzip.batch.common.logger
import com.blogzip.batch.config.OpenAiProperties
import com.blogzip.notification.common.SlackSender
import kotlinx.coroutines.delay
import org.springframework.stereotype.Component

@Component
class ArticleContentSummarizer(
    private val openAiProperties: OpenAiProperties,
    private val slackSender: SlackSender,
) {

    val log = logger()

    @OptIn(BetaOpenAI::class)
    suspend fun summarize(content: String): String? {
        val openAI = OpenAI(openAiProperties.apiKey)
        val threadId = ThreadId(openAiProperties.threadId)
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
            // todo model 지정
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
            return textContent.text.value
        } catch (e: Exception) {
            log.error("요약 실패. content = ${content.substring(0, 100)}...", e)
            slackSender.sendStackTraceAsync(SlackSender.SlackChannel.ERROR_LOG, e)
            return null
        }
    }
}