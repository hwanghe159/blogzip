package com.blogzip.batch.summarize

import com.blogzip.service.ArticleService
import kotlinx.coroutines.runBlocking
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
class SummarizeJobConfig(
    private val articleService: ArticleService,
    private val articleContentSummarizer: ArticleContentSummarizer
) {

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
                val articles = articleService.findAllBySummaryIsNull()
                for (article in articles) {
                    val summary =
                        runBlocking { articleContentSummarizer.summarize(article.content) }
                    article.summary = summary
                    article.summarizedBy = "gpt-3.5-turbo-0125"
                    articleService.save(article)
                }
                RepeatStatus.FINISHED
            }, platformTransactionManager)
            .build()
    }

}