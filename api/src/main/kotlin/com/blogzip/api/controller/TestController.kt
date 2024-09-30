package com.blogzip.api.controller

import com.blogzip.api.common.logger
import com.blogzip.crawler.service.RssFeedFetcher
import com.blogzip.notification.common.SlackSender
import org.springframework.web.bind.annotation.*
import org.springframework.web.reactive.function.client.WebClient
import java.lang.RuntimeException

@RestController
class TestController(
    private val slackSender: SlackSender,
    private val rssFeedFetcher: RssFeedFetcher,
) {

    val log = logger()

    @GetMapping("/api/v1/test/error-log/message")
    fun errorMessage() {
        log.error("에러발생")
    }

    @GetMapping("/api/v1/test/error-log/exception")
    fun errorException() {
        val e = RuntimeException("에러발생")
        throw e
    }

    @PostMapping("/api/v1/test/slack-message")
    fun slackMessage(@RequestBody message: String) {
        slackSender.sendMessageAsync(SlackSender.SlackChannel.ERROR_LOG, message)
    }

    @PostMapping("/api/v1/test/xml")
    fun fetchXmlTest(@RequestBody rss: String): String {
        return rssFeedFetcher.fetchXmlString(rss)
    }
}