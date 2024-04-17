package com.blogzip.batch.fetch

import com.blogzip.batch.common.logger
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

    var log = logger()

    @Bean
    fun fetchNewArticlesJob(
        jobRepository: JobRepository,
        platformTransactionManager: PlatformTransactionManager
    ): Job {
        return JobBuilder("fetch-new-articles", jobRepository)
            .incrementer(RunIdIncrementer())
            .start(fetchNewArticlesStep(jobRepository, platformTransactionManager))
            .build()
    }

    @Bean
    fun fetchNewArticlesStep(
        jobRepository: JobRepository,
        platformTransactionManager: PlatformTransactionManager
    ): Step {
        return StepBuilder("fetch-new-articles", jobRepository)
            .tasklet({ _, _ ->
//                val yesterday = LocalDate.of(2024, 3, 15).minusDays(1)
                val yesterday = LocalDate.now().minusDays(1)
                val blogs = blogService.findAll()
                for (blog in blogs) {
                    val articles = fetchArticles(blog, from = yesterday)
                    for (article in articles) {
                        articleService.save(article)
                    }
                }
                RepeatStatus.FINISHED
            }, platformTransactionManager)
            .build()
    }

    private fun fetchArticles(blog: Blog, from: LocalDate): List<Article> {
        when (blog.rssStatus) {

            WITH_CONTENT -> {
                return rssFeedFetcher.fetchArticles(blog.rss!!)
                    .filter { from <= it.createdDate }
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
                return rssFeedFetcher.fetchArticles(blog.rss!!)
                    .filter { from <= it.createdDate }
                    .mapNotNull {
                        val content = webScrapper.getContent(it.url)
                        if (content == null) {
                            null
                        } else
                            Article(
                                blog = blog,
                                title = it.title,
                                content = content,
                                url = it.url,
                                createdDate = it.createdDate
                            )
                    }
            }

            NO_RSS -> {
                if (blog.urlCssSelector == null) {
                    log.error("css selector가 없어 새 글 가져오기 실패. url=${blog.url}")
                    return emptyList()
                }
                val articles = webScrapper.getArticles(blog.url, blog.urlCssSelector!!)
                    .distinctBy { it.url }
                return articles
                    .mapNotNull {
                        val content = webScrapper.getContent(it.url)
                        if (content == null) {
                            null
                        } else {
                            Article(
                                blog = blog,
                                title = it.title,
                                content = content,
                                url = it.url,
                                // 신규 등록 블로그의 글은 createdDate=1970/01/01로 고정
                                createdDate = if (blog.isNew()) LocalDate.EPOCH else from,
                            )
                        }
                    }
            }
        }
    }
}