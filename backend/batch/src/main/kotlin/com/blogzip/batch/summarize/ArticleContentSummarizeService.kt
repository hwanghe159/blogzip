package com.blogzip.batch.summarize

import com.blogzip.ai.summary.ArticleContentSummarizer
import com.blogzip.ai.summary.ArticleToSummarize
import com.blogzip.logger
import com.blogzip.service.ArticleCommandService
import com.blogzip.service.ArticleQueryService
import com.blogzip.service.KeywordService
import com.blogzip.slack.SlackSender
import org.springframework.retry.RetryException
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class ArticleContentSummarizeService(
  private val articleQueryService: ArticleQueryService,
  private val articleCommandService: ArticleCommandService,
  private val articleContentSequentialSummarizer: ArticleContentSummarizer,
  private val keywordService: KeywordService,
  private val slackSender: SlackSender,
) {

  var log = logger()

  @Retryable(
    value = [RetryException::class],
    maxAttempts = 3,
    backoff = Backoff(delay = 1 * 1000)
  )
  fun summarize(startDate: LocalDate) {
    val articles = articleQueryService.findAllSummarizeTarget(startDate = startDate)
    val results = articleContentSequentialSummarizer
      .summarizeAndGetKeywordsAll(articles.map {
        ArticleToSummarize(
          id = it.id!!,
          content = it.content
        )
      })

    results.forEach {
      it.handle(
        onSuccess = { result ->
          articleCommandService.updateSummary(result.id, result.summary, result.summarizedBy)
          keywordService.addArticleKeywords(result.id, result.keywords)
        },
        onFailure = { articleId, throwable ->
          val exception = RuntimeException("$articleId 요약 실패", throwable)
          slackSender.sendStackTraceAsync(SlackSender.SlackChannel.ERROR_LOG, exception)
        }
      )
    }

    val message =
      "요약 결과: 총 ${articles.size}건, 성공 ${results.count { it.isSuccess() }}건, 실패 ${results.count { !it.isSuccess() }}건"
    log.warn(message)
    slackSender.sendMessageAsync(SlackSender.SlackChannel.MONITORING, message)
  }
}