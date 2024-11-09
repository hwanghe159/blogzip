package com.blogzip.slack

import com.slack.api.Slack
import com.slack.api.model.block.SectionBlock
import com.slack.api.model.block.composition.MarkdownTextObject
import com.slack.api.webhook.Payload
import com.slack.api.webhook.WebhookResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component

@Component
class SlackSender(
    private val slackProperties: SlackProperties,
    private val environment: Environment,
) {

    fun sendStackTraceAsync(channel: SlackChannel, throwable: Throwable) {
        if (!environment.activeProfiles.contains("prod")) {
            return
        }
        CoroutineScope(Dispatchers.Default).launch {
            callApi(
                slackProperties.webhookUrl,
                Payload.builder()
                    .channel("#${channel.channelName}")
                    .text(throwable.message)
                    .blocks(
                        listOf(
                            SectionBlock.builder()
                                .text(
                                    MarkdownTextObject.builder()
                                        .text(
                                            """
                                        ```${
                                                throwable.stackTraceToString()
                                                    .substring(0, 1000)
                                            }...
                                        ```
                                        """.trimIndent()
                                        )
                                        .build()
                                )
                                .build()
                        )
                    )
                    .build()
            )
        }
    }

    fun sendMessageAsync(channel: SlackChannel, message: String) {
        if (!environment.activeProfiles.contains("prod")) {
            return
        }
        val maxLength = 3997
        CoroutineScope(Dispatchers.Default).launch {
            callApi(
                slackProperties.webhookUrl,
                Payload.builder()
                    .channel("#${channel.channelName}")
                    .text(
                        if (message.length > maxLength) {
                            message.substring(0, maxLength) + "..."
                        } else message
                    )
                    .build()
            )
        }
    }

    private suspend fun callApi(url: String, payload: Payload): WebhookResponse {
        val slack = Slack.getInstance()
        return slack.send(url, payload)
    }

    enum class SlackChannel(val channelName: String) {
        ERROR_LOG("error-log"),
        MONITORING("monitoring")
    }
}