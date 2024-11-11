package com.blogzip.api.controller

import com.blogzip.ai.OpenAiApiClient
import com.blogzip.crawler.dto.Article
import com.blogzip.crawler.service.RssFeedFetcher
import com.blogzip.crawler.service.WebScrapper
import com.blogzip.logger
import com.blogzip.slack.SlackSender
import com.blogzip.notification.email.EmailSender
import org.springframework.web.bind.annotation.*
import java.lang.RuntimeException

@RestController
class TestController(
    private val slackSender: SlackSender,
    private val rssFeedFetcher: RssFeedFetcher,
    private val chromeWebScrapper: WebScrapper,
    private val emailSender: EmailSender,
    private val openAIApiClient: OpenAiApiClient,
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
    fun fetchXmlTest(@RequestBody rss: String): List<Article> {
        return rssFeedFetcher.getArticles(rss)
    }

    @PostMapping("/api/v1/test/crawler")
    fun crawlerTest(@RequestBody url: String): String? {
        return chromeWebScrapper.test(url)
    }

    @PostMapping("/api/v1/test/email")
    fun emailTest(@RequestBody request: Map<String, String>) {
        return emailSender.sendEmailUsingSES(
            request["to"]!!,
            request["subject"]!!,
            request["content"]!!
        )
    }

    @GetMapping("/api/v1/test/batch/{batchId}")
    fun getBatchStatus(@PathVariable batchId: String): Map<String, Any> {
        return openAIApiClient.getBatchStatus(batchId)
    }

    @GetMapping("/api/v1/test/file/{fileId}")
    fun getBatchResult(@PathVariable fileId: String): ByteArray {
        return openAIApiClient.getBatchResult(fileId)
    }
}