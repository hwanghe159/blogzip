package com.blogzip.batch.summarize

import com.blogzip.batch.common.JobResultNotifier
import com.blogzip.logger
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobScope
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager

@Configuration
class SummarizeJobConfig(
    private val jobResultNotifier: JobResultNotifier,
    private val summarizeTasklet: SummarizeTasklet,
) {

    val log = logger()

    companion object {
        private const val JOB_NAME = "summarize"
        const val PARAMETER_NAME = "start-date"
    }

    @Bean
    fun summarizeJob(
        jobRepository: JobRepository,
        platformTransactionManager: PlatformTransactionManager
    ): Job {
        return JobBuilder(JOB_NAME, jobRepository)
//            .incrementer(RunIdIncrementer())
            .start(summarizeStep(jobRepository, platformTransactionManager))
            .listener(jobResultNotifier)
            .build()
    }

    @JobScope
    @Bean
    fun summarizeStep(
        jobRepository: JobRepository,
        platformTransactionManager: PlatformTransactionManager
    ): Step {
        return StepBuilder("summarize", jobRepository)
            .tasklet(summarizeTasklet, platformTransactionManager)
            .allowStartIfComplete(true) // COMPLETED 상태로 끝났어도 재실행 가능
            .build()
    }

}