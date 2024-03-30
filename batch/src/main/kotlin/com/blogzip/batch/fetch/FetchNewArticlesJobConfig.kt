package com.blogzip.batch.fetch

import com.blogzip.crawler.service.ArticleFetcher
import com.blogzip.domain.Article
import com.blogzip.service.ArticleService
import com.blogzip.service.BlogService
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
class FetchNewArticlesJobConfig(
    private val blogService: BlogService,
    private val articleService: ArticleService,
    private val articleFetcher: ArticleFetcher,
) {

    @Bean
    fun fetchNewArticlesJob(
        jobRepository: JobRepository,
        platformTransactionManager: PlatformTransactionManager
    ): Job {
        return JobBuilder("fetch-new-articles", jobRepository)
            .start(fetchNewArticlesStep(jobRepository, platformTransactionManager))
            .incrementer(RunIdIncrementer())
            .build()
    }

    // todo 아예 작동을 안한다!! 확인해보기
    @Bean
    fun fetchNewArticlesStep(
        jobRepository: JobRepository,
        platformTransactionManager: PlatformTransactionManager
    ): Step {
        return StepBuilder("fetch-new-articles", jobRepository)
            .tasklet({ _, _ ->
                val today = LocalDate.of(2024, 3, 15)
                val blogs = blogService.findAll()
                val articles = blogs.filter { it.rss != null }
                    .flatMap { blog ->
                        articleFetcher.fetchArticles(blog.rss!!, today.minusDays(1))
                            .map { Article(blog = blog, title = it.title, url = it.url) }
                    }
                articleService.saveIfNotExists(articles)
                RepeatStatus.FINISHED
            }, platformTransactionManager)
            .build()
    }
}