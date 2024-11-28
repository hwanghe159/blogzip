package com.blogzip.api.controller

import com.blogzip.ai.summary.ArticleContentBatchSummarizer
import com.blogzip.ai.summary.OpenAiApiClient
import com.blogzip.crawler.dto.Article
import com.blogzip.crawler.dto.BlogMetadata
import com.blogzip.crawler.service.BlogMetadataScrapper
import com.blogzip.crawler.service.RssFeedFetcher
import com.blogzip.crawler.service.WebScrapper
import com.blogzip.domain.ArticleRepository
import com.blogzip.domain.BlogUrl
import com.blogzip.logger
import com.blogzip.slack.SlackSender
import com.blogzip.notification.email.EmailSender
import com.blogzip.service.ArticleQueryService
import org.springframework.web.bind.annotation.*
import java.lang.RuntimeException

@RestController
class TestController(
    private val slackSender: SlackSender,
    private val rssFeedFetcher: RssFeedFetcher,
    private val blogMetadataScrapper: BlogMetadataScrapper,
    private val emailSender: EmailSender,
    private val openAIApiClient: OpenAiApiClient,
    private val articleContentBatchSummarizer: ArticleContentBatchSummarizer,
    private val articleRepository: ArticleRepository,
    private val articleQueryService: ArticleQueryService,
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
    fun crawlerTest(@RequestBody url: String): BlogMetadata {
        val blogUrl = BlogUrl.from(url)
        return blogMetadataScrapper.getMetadata(blogUrl.toString())
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

    @GetMapping("/api/v1/test/summary/article/{articleIds}")
    fun summaryAndKeywordsTest(@PathVariable articleIds: List<Long>): List<ArticleContentBatchSummarizer.SummarizeAndKeywordsResult> {
        val map = articleQueryService.findAllById(articleIds)
            .map { ArticleContentBatchSummarizer.Article(id = it.id!!, content = it.content) }
        return articleContentBatchSummarizer.summarizeAndGetKeywordsAll(map)
    }
}