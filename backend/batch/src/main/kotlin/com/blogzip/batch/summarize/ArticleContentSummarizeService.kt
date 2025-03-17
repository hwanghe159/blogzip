package com.blogzip.batch.summarize

import com.blogzip.ai.summary.ArticleContentSummarizer
import com.blogzip.ai.summary.ArticleToSummarize
import com.blogzip.ai.summary.SummarizedArticleResult
import com.blogzip.slack.SlackSender
import com.blogzip.logger
import com.blogzip.service.ArticleCommandService
import com.blogzip.service.ArticleQueryService
import com.blogzip.service.KeywordService
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

    results
      .filter { it.result == SummarizedArticleResult.Result.SUCCESS }
      .forEach {
        val result = it.article!!
        articleCommandService.updateSummary(result.id, result.summary, result.summarizedBy)
        keywordService.addArticleKeywords(result.id, result.keywords)
      }

    val message = "요약 결과: 총 ${articles.size}건, 성공 ${results.count { it.result == SummarizedArticleResult.Result.SUCCESS }}건, 실패 ${results.count { it.result == SummarizedArticleResult.Result.FAIL }}건"
    log.warn(message)
    slackSender.sendMessageAsync(SlackSender.SlackChannel.MONITORING, message)
  }
}