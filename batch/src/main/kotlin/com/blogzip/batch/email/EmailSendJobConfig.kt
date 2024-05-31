package com.blogzip.batch.email

import com.blogzip.batch.common.JobResultListener
import com.blogzip.batch.common.logger
import com.blogzip.notification.common.SlackSender
import com.blogzip.notification.common.SlackSender.SlackChannel.ERROR_LOG
import com.blogzip.notification.email.Article
import com.blogzip.notification.email.EmailSender
import com.blogzip.notification.email.User
import com.blogzip.service.ArticleService
import com.blogzip.service.UserService
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.launch.support.RunIdIncrementer
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
    private val articleService: ArticleService,
    private val jobResultListener: JobResultListener,
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
            .start(emailSendStep(jobRepository, platformTransactionManager))
            .incrementer(RunIdIncrementer())
            .listener(jobResultListener)
            .build()
    }

    @Bean
    fun emailSendStep(
        jobRepository: JobRepository,
        platformTransactionManager: PlatformTransactionManager,
    ): Step {
        return StepBuilder("email-send", jobRepository)
            .tasklet({ _, _ ->
                val today = LocalDate.now()
                val users = userService.findAllByDayOfWeek(today.dayOfWeek)
                for (user in users) {
                    val accumulatedDates = user.getAccumulatedDates(today)
                    val newArticles = user.subscriptions
                        .flatMap { it.blog.articles.filter { accumulatedDates.contains(it.createdDate) } }
                    emailSender.sendNewArticles(
                        User(
                            email = user.email,
                            receiveDates = accumulatedDates,
                        ),
                        newArticles
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
                                    blogName = it.blog.name,
                                    createdDate = it.createdDate!!,
                                )
                            })
                }
                RepeatStatus.FINISHED
            }, platformTransactionManager)
            .build()
    }
}