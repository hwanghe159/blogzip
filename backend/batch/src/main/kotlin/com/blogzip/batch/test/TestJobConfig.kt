package com.blogzip.batch.test

import com.blogzip.logger
import com.blogzip.slack.SlackSender
import com.blogzip.slack.SlackSender.SlackChannel.MONITORING
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager

@Configuration
class TestJobConfig(
    private val slackSender: SlackSender,
) {

    var log = logger()

    companion object {
        private const val JOB_NAME = "test"
    }

    @Bean
    fun testJob(
        jobRepository: JobRepository,
        platformTransactionManager: PlatformTransactionManager
    ): Job {
        return JobBuilder(JOB_NAME, jobRepository)
            .start(testStep(jobRepository, platformTransactionManager))
            .build()
    }

    @Bean
    fun testStep(
        jobRepository: JobRepository,
        platformTransactionManager: PlatformTransactionManager
    ): Step {
        return StepBuilder("test", jobRepository)
            .tasklet({ _, _ ->
                slackSender.sendMessageAsync(MONITORING, "테스트 배치 실행 완료.")
                RepeatStatus.FINISHED
            }, platformTransactionManager)
            .allowStartIfComplete(true) // COMPLETED 상태로 끝났어도 재실행 가능
            .build()
    }
}