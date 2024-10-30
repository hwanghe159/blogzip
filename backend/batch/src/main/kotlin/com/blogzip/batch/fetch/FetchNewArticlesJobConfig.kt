package com.blogzip.batch.fetch

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
class FetchNewArticlesJobConfig(
    private val fetchNewArticlesTasklet: FetchNewArticlesTasklet,
    private val jobResultNotifier: JobResultNotifier,
    private val webScrapperDestroyer: WebScrapperDestroyer,
) {

    val log = logger()

    companion object {
        private const val JOB_NAME = "fetch-new-articles"
        const val PARAMETER_NAME = "target-date"
    }

    @Bean
    fun fetchNewArticlesJob(
        jobRepository: JobRepository,
        platformTransactionManager: PlatformTransactionManager
    ): Job {
        return JobBuilder(JOB_NAME, jobRepository)
//            .incrementer(RunIdIncrementer())
            .start(fetchNewArticlesStep(jobRepository, platformTransactionManager))
            .listener(jobResultNotifier)
            .build()
    }

    @JobScope
    @Bean
    fun fetchNewArticlesStep(
        jobRepository: JobRepository,
        platformTransactionManager: PlatformTransactionManager
    ): Step {
        return StepBuilder("fetch-new-articles", jobRepository)
            .tasklet(fetchNewArticlesTasklet, platformTransactionManager)
            .listener(webScrapperDestroyer)
            .allowStartIfComplete(true) // COMPLETED 상태로 끝났어도 재실행 가능
            .build()
    }
}