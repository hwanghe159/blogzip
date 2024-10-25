package com.blogzip.batch.fetch

import com.blogzip.batch.common.JobResultNotifier
import com.blogzip.batch.common.getParameter
import com.blogzip.batch.common.logger
import com.blogzip.crawler.service.RssFeedFetcher
import com.blogzip.crawler.service.WebScrapper
import com.blogzip.crawler.service.XmlParser
import com.blogzip.domain.Blog
import com.blogzip.domain.Blog.RssStatus.*
import com.blogzip.notification.common.SlackSender
import com.blogzip.notification.common.SlackSender.SlackChannel.ERROR_LOG
import com.blogzip.service.ArticleCommandService
import com.blogzip.service.ArticleQueryService
import com.blogzip.service.BlogService
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager
import java.lang.Exception
import java.time.LocalDate

@Configuration
class FetchNewArticlesJobConfig(
    private val blogService: BlogService,
    private val articleQueryService: ArticleQueryService,
    private val articleCommandService: ArticleCommandService,
    private val jobResultNotifier: JobResultNotifier,
    private val rssFeedFetcher: RssFeedFetcher,
    private val webScrapper: WebScrapper,
    private val slackSender: SlackSender,
    private val xmlParser: XmlParser,
) {

    var log = logger()

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

    @Bean
    fun fetchNewArticlesStep(
        jobRepository: JobRepository,
        platformTransactionManager: PlatformTransactionManager
    ): Step {
        return StepBuilder("fetch-new-articles", jobRepository)
            .tasklet({ _, chunkContext ->
                val parameter = chunkContext.getParameter(PARAMETER_NAME)
                val targetDate: LocalDate
                if (parameter.isNullOrBlank()) {
                    val yesterday = LocalDate.now().minusDays(1)
                    targetDate = yesterday
                } else {
                    targetDate = LocalDate.parse(parameter)
                }
                val blogs = blogService.findAll().shuffled() // 동일 시간 요청에 의한 IP 차단 방지
                for (blog in blogs) {
                    val processCount = blogs.indexOf(blog) + 1
                    val totalCount = blogs.size
                    log.info("${blog.url} 의 새 글 가져오는중. (${processCount}/${totalCount})")
                    val articles = fetchArticles(blog, from = targetDate)
                    for (article in articles) {
                        articleCommandService.save(article)
                    }
                }
                RepeatStatus.FINISHED
            }, platformTransactionManager)
            .allowStartIfComplete(true) // COMPLETED 상태로 끝났어도 재실행 가능
            .build()
    }

    private fun fetchArticles(blog: Blog, from: LocalDate): List<com.blogzip.domain.Article> {
        return when (blog.rssStatus) {

            WITH_CONTENT -> {
                if (blog.rss == null) {
                    val errorMessage = "blog.rss가 없어 새 글 가져오기 실패. blog.id=${blog.id}"
                    log.error(errorMessage)
                    slackSender.sendMessageAsync(channel = ERROR_LOG, errorMessage)
                    return emptyList()
                }

                var articles: List<com.blogzip.crawler.dto.Article> = emptyList()
                try {
                    val xmlString = rssFeedFetcher.fetchXmlString(blog.rss!!)
                    articles = xmlParser.convertToArticles(xmlString)
                } catch (e: Exception) {
                    log.error("${blog.rss}의 글 가져오기 실패.", e)
                    slackSender.sendStackTraceAsync(channel = ERROR_LOG, e)
                }

                articles.filter {
                    if (it.createdDate == null) {
                        true
                    } else {
                        from <= it.createdDate
                    }
                }
                    .map {
                        com.blogzip.domain.Article(
                            blogId = blog.id!!,
                            title = it.title,
                            content = it.content!!,
                            url = it.url,
                            // RSS 안에 글 작성 날짜가 주어지지 않는 경우도 있음
                            createdDate =
                            if (it.createdDate == null) {
                                if (blog.isNew()) {
                                    LocalDate.EPOCH
                                } else {
                                    LocalDate.now()
                                }
                            } else {
                                it.createdDate
                            }
                        )
                    }
            }

            WITHOUT_CONTENT -> {
                if (blog.rss == null) {
                    val errorMessage = "blog.rss가 없어 새 글 가져오기 실패. blog.id=${blog.id}"
                    log.error(errorMessage)
                    slackSender.sendMessageAsync(channel = ERROR_LOG, errorMessage)
                    return emptyList()
                }

                var articles: List<com.blogzip.crawler.dto.Article> = emptyList()
                try {
                    val xmlString = rssFeedFetcher.fetchXmlString(blog.rss!!)
                    articles = xmlParser.convertToArticles(xmlString)
                } catch (e: Exception) {
                    log.error("${blog.rss}의 글 가져오기 실패.", e)
                    slackSender.sendStackTraceAsync(channel = ERROR_LOG, e)
                }

                articles.filterNot { articleQueryService.existsByUrl(it.url) }
                    .filter {
                        if (it.createdDate == null) {
                            true
                        } else {
                            from <= it.createdDate
                        }
                    }
                    .mapNotNull {
                        val content = webScrapper.getContent(it.url)
                        if (content.isNullOrBlank()) {
                            slackSender.sendMessageAsync(
                                channel = ERROR_LOG,
                                "글 크롤링 실패. url=${it.url}"
                            )
                            null
                        } else
                            com.blogzip.domain.Article(
                                blogId = blog.id!!,
                                title = it.title,
                                content = content,
                                url = it.url,
                                createdDate =
                                if (it.createdDate == null) {
                                    if (blog.isNew()) {
                                        LocalDate.EPOCH
                                    } else {
                                        LocalDate.now()
                                    }
                                } else {
                                    it.createdDate
                                }
                            )
                    }
            }

            NO_RSS -> {
                if (blog.urlCssSelector == null) {
                    val errorMessage = "css selector가 없어 새 글 가져오기 실패. url=${blog.url}"
                    log.error(errorMessage)
                    slackSender.sendMessageAsync(channel = ERROR_LOG, errorMessage)
                    return emptyList()
                }
                val scrapResult = webScrapper.getArticles(blog.url, blog.urlCssSelector!!)
                val articles = scrapResult.articles.distinctBy { it.url }
                if (articles.isEmpty()) {
                    slackSender.sendMessageAsync(ERROR_LOG, "${blog.url} 크롤링 실패")
                    slackSender.sendStackTraceAsync(ERROR_LOG, scrapResult.failCause!!)
                }
                return articles
                    .filterNot { articleQueryService.existsByUrl(it.url) }
                    .mapNotNull {
                        val content = webScrapper.getContent(it.url)
                        if (content == null) {
                            null
                        } else {
                            com.blogzip.domain.Article(
                                blogId = blog.id!!,
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