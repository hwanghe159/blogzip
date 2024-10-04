package com.blogzip.batch.summarize

import com.blogzip.batch.common.JobResultListener
import com.blogzip.batch.common.getParameter
import com.blogzip.batch.summarize.SummarizeJobConfig.Companion.PARAMETER_NAME
import com.blogzip.crawler.common.logger
import com.blogzip.notification.common.SlackSender
import com.blogzip.service.ArticleService
import kotlinx.coroutines.runBlocking
import org.springframework.batch.core.StepContribution
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.retry.RetryException
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class SummarizeTasklet(
    private val articleService: ArticleService,
    private val articleContentSummarizer: ArticleContentSummarizer,
    private val slackSender: SlackSender,
) : Tasklet {

    val log = logger()

    // todo 재시도 로직 테스트해보기
//    @Retryable(
//        value = [RetryException::class],
//        maxAttempts = 3,  // 최대 3번 재시도
//        backoff = Backoff(delay = 5 * 60 * 1000)  // 5분 대기 후 재시도
//    )
    override fun execute(
        contribution: StepContribution,
        chunkContext: ChunkContext
    ): RepeatStatus? {
        val parameter = chunkContext.getParameter(PARAMETER_NAME)
        val targetDate: LocalDate
        if (parameter.isNullOrBlank()) {
            val yesterday = LocalDate.now().minusDays(1)
            targetDate = yesterday
        } else {
            targetDate = LocalDate.parse(parameter)
        }

        val articles = articleService.findAllSummarizeTarget(createdDate = targetDate)
        var failCount = 0
        for (article in articles) {
            runBlocking {
                val summarizeResult = articleContentSummarizer.summarize(article.content)
                if (summarizeResult != null) {
                    article.summary = summarizeResult.summary
                    article.summarizedBy = summarizeResult.summarizedBy
                } else {
                    failCount++
                }
            }
        }

        val message =
            "요약 결과: 총 ${articles.size}건, 성공 ${articles.size - failCount}건, 실패 ${failCount}건"
        log.warn(message)
        slackSender.sendMessageAsync(SlackSender.SlackChannel.MONITORING, message)

        return RepeatStatus.FINISHED
//        return if (failCount != 0) {
//            val retryMessage = "실패 ${failCount}건이 존재합니다. 5분 후 재시도합니다."
//            slackSender.sendMessageAsync(SlackSender.SlackChannel.MONITORING, retryMessage)
//            throw RetryException(retryMessage)
//        } else {
//            RepeatStatus.FINISHED
//        }
    }
}