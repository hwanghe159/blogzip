package com.blogzip.batch.summarize

import com.blogzip.crawler.common.logger
import com.blogzip.service.ArticleService
import kotlinx.coroutines.runBlocking
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
class SummarizeJobConfig(
    private val articleService: ArticleService,
    private val articleContentSummarizer: ArticleContentSummarizer
) {

    val log = logger()

    @Bean
    fun summarizeJob(
        jobRepository: JobRepository,
        platformTransactionManager: PlatformTransactionManager
    ): Job {
        return JobBuilder("summarize", jobRepository)
            .incrementer(RunIdIncrementer())
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
                val yesterday = LocalDate.now().minusDays(1)
//                val yesterday = LocalDate.of(2024, 4, 5)
                val articles = articleService.findAllSummarizeTarget(createdDate = yesterday)
                for (article in articles) {
                    runBlocking {
                        val summary = articleContentSummarizer.summarize(article.content)
                        article.summary = summary
                        article.summarizedBy = "gpt-3.5-turbo-0125"
                        articleService.save(article)
                    }
                }
                log.info("요약 성공. 총 ${articles.size}건")
                RepeatStatus.FINISHED
            }, platformTransactionManager)
            .build()
    }

}