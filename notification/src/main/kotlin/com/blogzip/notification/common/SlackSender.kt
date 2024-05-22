package com.blogzip.notification.common

import com.blogzip.notification.config.SlackProperties
import com.slack.api.Slack
import com.slack.api.model.block.SectionBlock
import com.slack.api.model.block.composition.MarkdownTextObject
import com.slack.api.webhook.Payload
import com.slack.api.webhook.WebhookResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springframework.stereotype.Component

@Component
class SlackSender(
    private val slackProperties: SlackProperties,
) {

    fun sendStackTraceAsync(channel: SlackChannel, throwable: Throwable) {
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
        CoroutineScope(Dispatchers.Default).launch {
            callApi(
                slackProperties.webhookUrl,
                Payload.builder()
                    .channel("#${channel.channelName}")
                    .text(message)
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