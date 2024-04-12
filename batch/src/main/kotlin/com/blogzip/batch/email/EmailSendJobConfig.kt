package com.blogzip.batch.email

import com.blogzip.batch.common.logger
import com.blogzip.notification.email.Article
import com.blogzip.notification.email.EmailSender
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
    private val emailSender: EmailSender
) {

    val log = logger()

    @Bean
    fun emailSendJob(
        jobRepository: JobRepository,
        platformTransactionManager: PlatformTransactionManager
    ): Job {
        return JobBuilder("email-send", jobRepository)
            .start(emailSendStep(jobRepository, platformTransactionManager))
            .incrementer(RunIdIncrementer())
            .build()
    }

    @Bean
    fun emailSendStep(
        jobRepository: JobRepository,
        platformTransactionManager: PlatformTransactionManager
    ): Step {
        return StepBuilder("email-send", jobRepository)
            .tasklet({ _, _ ->
                val yesterday = LocalDate.now().minusDays(1)
                val users = userService.findAll()
                val newArticles = articleService.findAllByCreatedDate(yesterday) // todo 변경
                for (user in users) {
//                    val newArticles =
//                        articleService.findAllByUserAndCreatedDate(user, yesterday)
                    emailSender.sendNewArticles(
                        user.email,
                        newArticles
                            .filter {
                                if (it.summary == null) {
                                    log.error("요약되지 않아 전송 과정에서 걸러짐. article.id=${it.id}")
                                }
                                it.summary != null
                            }
                            .map {
                                Article(
                                    title = it.title,
                                    url = it.url,
                                    summary = it.summary!!,
                                )
                            })
                }
                RepeatStatus.FINISHED
            }, platformTransactionManager)
            .build()
    }
}