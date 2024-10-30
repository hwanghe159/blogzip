package com.blogzip.batch.email

import com.blogzip.batch.common.JobResultNotifier
import com.blogzip.logger
import com.blogzip.slack.SlackSender
import com.blogzip.slack.SlackSender.SlackChannel.ERROR_LOG
import com.blogzip.notification.email.Article
import com.blogzip.notification.email.EmailSender
import com.blogzip.notification.email.User
import com.blogzip.service.ArticleQueryService
import com.blogzip.service.BlogService
import com.blogzip.service.UserService
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobScope
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager
import java.time.LocalDate

@Configuration
class EmailSendJobConfig(
    private val userService: UserService,
    private val articleQueryService: ArticleQueryService,
    private val blogService: BlogService,
    private val jobResultNotifier: JobResultNotifier,
    private val emailSender: EmailSender,
    private val slackSender: SlackSender,
) {

    val log = logger()

    companion object {
        private const val JOB_NAME = "email-send"
    }

    @Bean
    fun emailSendJob(
        jobRepository: JobRepository,
        platformTransactionManager: PlatformTransactionManager
    ): Job {
        return JobBuilder(JOB_NAME, jobRepository)
//            .incrementer(RunIdIncrementer())
            .start(emailSendStep(jobRepository, platformTransactionManager))
            .listener(jobResultNotifier)
            .build()
    }

    @JobScope
    @Bean
    fun emailSendStep(
        jobRepository: JobRepository,
        platformTransactionManager: PlatformTransactionManager,
    ): Step {
        return StepBuilder("email-send", jobRepository)
            .tasklet({ _, _ ->
                val today = LocalDate.now()
                val users = userService.findAllByDayOfWeek(today.dayOfWeek)
                val blogs = blogService.findAll().map { it.id to it }.toMap()

                for (user in users) {
                    val accumulatedDates = user.getAccumulatedDates(today)
                    val blogIds = user.getAllSubscribingBlogIds()
                    val newArticles =
                        articleQueryService.findAllByBlogIdsAndCreatedDates(
                            blogIds,
                            accumulatedDates
                        )
                    emailSender.sendNewArticles(
                        to = User(
                            email = user.email,
                            receiveDates = accumulatedDates,
                        ),
                        articles = newArticles
                            .filter {
                                if (it.summary == null) {
                                    val errorMessage = "요약되지 않아 전송 과정에서 걸러짐. article.id=${it.id}"
                                    log.error(errorMessage)
                                    slackSender.sendMessageAsync(channel = ERROR_LOG, errorMessage)
                                }
                                it.summary != null
                            }
                            .map {
                                Article(
                                    title = it.title,
                                    url = it.url,
                                    summary = it.summary!!,
                                    blogName = blogs[it.blogId]?.name!!,
                                    createdDate = it.createdDate!!,
                                )
                            })
                }
                RepeatStatus.FINISHED
            }, platformTransactionManager)
            .allowStartIfComplete(true) // COMPLETED 상태로 끝났어도 재실행 가능
            .build()
    }
}