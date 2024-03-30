package com.blogzip.batch.summarize

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
class SummarizeJobConfig {

    @Bean
    fun summarizeJob(
        jobRepository: JobRepository,
        platformTransactionManager: PlatformTransactionManager
    ): Job {
        return JobBuilder("summarize", jobRepository)
            .start(summarizeStep(jobRepository, platformTransactionManager))
            .build()
    }

    @Bean
    fun summarizeStep(
        jobRepository: JobRepository,
        platformTransactionManager: PlatformTransactionManager
    ): Step {
        return StepBuilder("summarize", jobRepository)
            .tasklet({ _, _ ->

                println("ㅋㅋㅋ 요약한다!!")

                RepeatStatus.FINISHED
            }, platformTransactionManager)
            .build()
    }

}