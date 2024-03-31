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
import kotlinx.coroutines.delay
import org.springframework.stereotype.Component

@Component
class ArticleContentSummarizer(private val openAiProperties: OpenAiProperties) {

    @OptIn(BetaOpenAI::class)
    suspend fun summarize(content: String) {
        val openAI = OpenAI(openAiProperties.apiKey)
        val threadId = ThreadId(openAiProperties.threadId)
        val assistantId = AssistantId(openAiProperties.assistantId)
        openAI.message(
            threadId = threadId,
            request = MessageRequest(
                role = Role.User,
                content = content
            )
        )
        val run = openAI.createRun(
            threadId,
            request = RunRequest(
                assistantId = assistantId
            )
        )
        do {
            delay(1500)
            val retrievedRun = openAI.getRun(threadId = threadId, runId = run.id)
        } while (retrievedRun.status != Status.Completed)
        val messages = openAI.messages(threadId)
        println("\nThe assistant's response:")
        for (message in messages) {
            val textContent = message.content.first() as? MessageContent.Text ?: error("Expected MessageContent.Text")
            println(textContent.text.value)
        }
        return
    }
}