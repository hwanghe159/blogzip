package com.blogzip.api.controller

import com.blogzip.ai.summary.ArticleContentBatchSummarizer
import com.blogzip.ai.summary.OpenAiApiClient
import com.blogzip.crawler.dto.Article
import com.blogzip.crawler.service.SeleniumBlogMetadataScrapper
import com.blogzip.crawler.service.RssFeedFetcher
import com.blogzip.domain.ArticleRepository
import com.blogzip.domain.BlogUrl
import com.blogzip.logger
import com.blogzip.slack.SlackSender
import com.blogzip.notification.email.EmailSender
import com.blogzip.service.ArticleQueryService
import com.blogzip.service.BlogMetadataScrapper
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.web.bind.annotation.*
import java.lang.RuntimeException

@RestController
class TestController(
    private val slackSender: SlackSender,
    private val rssFeedFetcher: RssFeedFetcher,
    private val seleniumBlogMetadataScrapper: SeleniumBlogMetadataScrapper,
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
    fun crawlerTest(@RequestBody url: String): BlogMetadataScrapper.BlogMetadata {
        val blogUrl = BlogUrl.from(url)
        return seleniumBlogMetadataScrapper.getMetadata(blogUrl.toString())
    }

    // todo 제거
    @Scheduled(fixedDelay = 10 * 60 * 1000)
    fun crawlerSessionTest() {
        try {
            val blogUrl = BlogUrl.from("https://google.com/")
            seleniumBlogMetadataScrapper.getMetadata(blogUrl.toString())
        } catch (e: Exception) {
            log.error("메타데이터 조회 실패", e)
            slackSender.sendMessageAsync(SlackSender.SlackChannel.ERROR_LOG, "메타데이터 조회 실패")
        }
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