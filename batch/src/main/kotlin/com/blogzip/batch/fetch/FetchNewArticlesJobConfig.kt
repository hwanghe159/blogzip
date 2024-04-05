package com.blogzip.batch.fetch

import com.blogzip.crawler.service.RssFeedFetcher
import com.blogzip.crawler.service.WebScrapper
import com.blogzip.domain.Article
import com.blogzip.domain.Blog
import com.blogzip.domain.Blog.RssStatus.*
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
    private val rssFeedFetcher: RssFeedFetcher,
    private val webScrapper: WebScrapper,
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

    @Bean
    fun fetchNewArticlesStep(
        jobRepository: JobRepository,
        platformTransactionManager: PlatformTransactionManager
    ): Step {
        return StepBuilder("fetch-new-articles", jobRepository)
            .tasklet({ _, _ ->
                val yesterday = LocalDate.of(2024, 3, 15).minusDays(1)
//                val yesterday = LocalDate.now().minusDays(1)
                val blogs = blogService.findAll()
                blogs.flatMap { blog ->
                    val articles = fetchArticles(blog, from = yesterday)
                    articleService.saveIfNotExists(articles)
                }
                RepeatStatus.FINISHED
            }, platformTransactionManager)
            .build()
    }

    private fun fetchArticles(blog: Blog, from: LocalDate): List<Article> {
        when (blog.rssStatus) {

            WITH_CONTENT -> {
                return rssFeedFetcher.fetchContents(blog.rss!!, from)
                    .map {
                        Article(
                            blog = blog,
                            title = it.title,
                            content = it.content!!,
                            url = it.url,
                            createdDate = it.createdDate
                        )
                    }
            }

            WITHOUT_CONTENT -> {
                return rssFeedFetcher.fetchContents(blog.rss!!, from)
                    .map {
                        Article(
                            blog = blog,
                            title = it.title,
                            content = webScrapper.getContent(it.url),
                            url = it.url,
                            createdDate = it.createdDate
                        )
                    }
            }

            NO_RSS -> {
                val articles = webScrapper.getArticles(blog.url, blog.urlCssSelector!!)
                return articles.map {
                    Article(
                        blog = blog,
                        title = it.title,
                        content = webScrapper.getContent(it.url),
                        url = it.url,
                        createdDate = from, // 직접 크롤링은 생성날짜를 알기 어려움. from 으로 고정
                    )
                }
            }
        }
    }
}