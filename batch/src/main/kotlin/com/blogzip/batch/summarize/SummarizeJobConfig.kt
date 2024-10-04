package com.blogzip.batch.summarize

import com.blogzip.batch.common.JobResultListener
import com.blogzip.crawler.common.logger
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager

@Configuration
class SummarizeJobConfig(
    private val jobResultListener: JobResultListener,
    private val summarizeTasklet: SummarizeTasklet,
) {

    val log = logger()

    companion object {
        private const val JOB_NAME = "summarize"
        const val PARAMETER_NAME = "target-date"
    }

    @Bean
    fun summarizeJob(
        jobRepository: JobRepository,
        platformTransactionManager: PlatformTransactionManager
    ): Job {
        return JobBuilder(JOB_NAME, jobRepository)
            .incrementer(RunIdIncrementer())
            .start(summarizeStep(jobRepository, platformTransactionManager))
            .listener(jobResultListener)
            .build()
    }

    @Bean
    fun summarizeStep(
        jobRepository: JobRepository,
        platformTransactionManager: PlatformTransactionManager
    ): Step {
        return StepBuilder("summarize", jobRepository)
            .tasklet(summarizeTasklet, platformTransactionManager)
            .build()
    }

}