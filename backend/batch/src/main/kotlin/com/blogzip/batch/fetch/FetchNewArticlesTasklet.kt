package com.blogzip.batch.fetch

import com.blogzip.batch.common.getParameter
import com.blogzip.batch.fetch.FetchNewArticlesJobConfig.Companion.PARAMETER_NAME
import com.blogzip.crawler.service.HtmlCompressor
import com.blogzip.crawler.service.RssFeedFetcher
import com.blogzip.crawler.service.WebScrapper
import com.blogzip.domain.Blog
import com.blogzip.domain.Blog.RssStatus.*
import com.blogzip.logger
import com.blogzip.service.ArticleCommandService
import com.blogzip.service.ArticleQueryService
import com.blogzip.service.BlogService
import com.blogzip.slack.SlackSender
import com.blogzip.slack.SlackSender.SlackChannel.ERROR_LOG
import org.springframework.batch.core.StepContribution
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.stereotype.Component
import java.lang.Exception
import java.time.LocalDate

@Component
class FetchNewArticlesTasklet(
    private val blogService: BlogService,
    private val articleCommandService: ArticleCommandService,
    private val withContentFetcher: NewArticlesFetcher,
    private val withoutContentFetcher: NewArticlesFetcher,
    private val noRssContentFetcher: NewArticlesFetcher,
) : Tasklet {

    val log = logger()

    override fun execute(
        contribution: StepContribution,
        chunkContext: ChunkContext
    ): RepeatStatus? {
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

            val articles = when (blog.rssStatus) {
                WITH_CONTENT -> withContentFetcher.fetchArticles(blog, from = targetDate)
                WITHOUT_CONTENT -> withoutContentFetcher.fetchArticles(blog, from = targetDate)
                NO_RSS -> noRssContentFetcher.fetchArticles(blog, from = targetDate)
            }

            for (article in articles) {
                articleCommandService.save(article)
            }
        }
        return RepeatStatus.FINISHED
    }
}