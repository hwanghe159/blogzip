package com.blogzip.batch.fetch

import com.blogzip.crawler.service.HtmlCompressor
import com.blogzip.crawler.service.RssFeedFetcher
import com.blogzip.domain.Article
import com.blogzip.domain.Blog
import com.blogzip.logger
import com.blogzip.slack.SlackSender
import com.blogzip.slack.SlackSender.SlackChannel.ERROR_LOG
import org.springframework.stereotype.Component
import java.lang.Exception
import java.time.LocalDate

@Component
class WithContentFetcher(
    private val slackSender: SlackSender,
    private val rssFeedFetcher: RssFeedFetcher,
    private val htmlCompressor: HtmlCompressor,
) : NewArticlesFetcher {

    val log = logger()

    override fun fetchArticles(blog: Blog, from: LocalDate): List<Article> {
        if (blog.rss == null) {
            val errorMessage = "blog.rss가 없어 새 글 가져오기 실패. blog.id=${blog.id}"
            log.error(errorMessage)
            slackSender.sendMessageAsync(channel = ERROR_LOG, errorMessage)
            return emptyList()
        }

        var articles: List<com.blogzip.crawler.dto.Article> = emptyList()
        try {
            articles = rssFeedFetcher.getArticles(blog.rss!!)
        } catch (e: Exception) {
            log.error("${blog.rss}의 글 가져오기 실패.", e)
            slackSender.sendStackTraceAsync(channel = ERROR_LOG, e)
        }

        return articles.filter {
            if (it.createdDate == null) {
                true
            } else {
                from <= it.createdDate
            }
        }
            .map {
                Article(
                    blogId = blog.id!!,
                    title = it.title,
                    content = htmlCompressor.compress(it.content!!),
                    url = it.url,

                    // RSS 안에 글 작성 날짜가 주어지지 않는 경우도 있음
                    createdDate =
                    if (it.createdDate == null) {
                        if (blog.isNew()) {
                            LocalDate.EPOCH
                        } else {
                            LocalDate.now().minusDays(1)
                        }
                    } else {
                        it.createdDate
                    }
                )
            }
    }
}