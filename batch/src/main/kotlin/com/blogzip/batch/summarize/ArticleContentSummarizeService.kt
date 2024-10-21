package com.blogzip.batch.summarize

import com.blogzip.batch.common.logger
import com.blogzip.notification.common.SlackSender
import com.blogzip.service.ArticleService
import kotlinx.coroutines.runBlocking
import org.springframework.retry.RetryException
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class ArticleContentSummarizeService(
    private val articleService: ArticleService,
    private val articleContentSummarizer: ArticleContentSummarizer,
    private val slackSender: SlackSender,
) {

    var log = logger()

    @Retryable(
        value = [RetryException::class],
        maxAttempts = 3,
        backoff = Backoff(delay = 1 * 1000)
    )
    fun summarize(startDate: LocalDate) {
        val articles = articleService.findAllSummarizeTarget(startDate = startDate)
        var failCount = 0
        for (article in articles) {
            runBlocking {
                val summarizeResult = articleContentSummarizer.summarize(article.content)
                if (summarizeResult != null) {
                    articleService.updateSummary(
                        article.id!!,
                        summarizeResult.summary,
                        summarizeResult.summarizedBy
                    )
                } else {
                    failCount++
                }
            }
        }

        val message =
            "요약 결과: 총 ${articles.size}건, 성공 ${articles.size - failCount}건, 실패 ${failCount}건"
        log.warn(message)
        slackSender.sendMessageAsync(SlackSender.SlackChannel.MONITORING, message)

        if (failCount != 0) {
            val retryMessage = "실패 ${failCount}건이 존재합니다. 5분 후 재시도합니다."
            slackSender.sendMessageAsync(SlackSender.SlackChannel.MONITORING, retryMessage)
            throw RetryException(retryMessage)
        }
    }
}