package com.blogzip.service

import com.blogzip.common.DomainException
import com.blogzip.common.ErrorCode
import com.blogzip.domain.Blog
import com.blogzip.domain.BlogUrl
import com.blogzip.slack.SlackSender
import com.blogzip.slack.SlackSender.SlackChannel.MONITORING
import org.springframework.stereotype.Service
import java.net.URISyntaxException

@Service
class BlogCreateService(
    private val blogService: BlogService,
    private val blogMetadataScrapper: BlogMetadataScrapper,
    private val slackSender: SlackSender,
) {

    fun create(url: String, userId: Long): BlogCreateResult {
        val blogUrl = try {
            BlogUrl.from(url)
        } catch (e: URISyntaxException) {
            throw DomainException(ErrorCode.BLOG_URL_NOT_VALID)
        }
        if (blogService.existsByUrl(blogUrl)) {
            throw DomainException(ErrorCode.BLOG_URL_DUPLICATED)
        }
        val metadata = blogMetadataScrapper.getMetadata(blogUrl.toString())
        if (metadata.imageUrl == null || metadata.rss == null) {
            slackSender.sendMessageAsync(
                MONITORING,
                "imageUrl==null 또는 rss==null. url=$blogUrl, metadata=$metadata"
            )
        }

        // todo rss 가 있어도 cloudflare에 의해 차단되는 경우가 있음. 이 경우엔 NO_RSS 가 되어야 함
        val rssStatus =
            if (metadata.rss == null) Blog.RssStatus.NO_RSS
            else if (metadata.isContentContainsInRss) Blog.RssStatus.WITH_CONTENT
            else Blog.RssStatus.WITHOUT_CONTENT

        if (rssStatus == Blog.RssStatus.NO_RSS) {
            slackSender.sendMessageAsync(MONITORING, "url_css_selector 직접 추가 필요. url=$blogUrl")
        }
        val blog = blogService.save(
            name = metadata.title,
            url = blogUrl.toString(),
            image = metadata.imageUrl,
            rss = metadata.rss,
            rssStatus = rssStatus,
            createdBy = userId,
        )
        return BlogCreateResult(
            id = blog.id!!,
            name = blog.name,
            url = blog.url,
            image = blog.image,
            rss = blog.rss,
        )
    }

    data class BlogCreateResult(
        val id: Long,
        val name: String,
        val url: String,
        val image: String?,
        val rss: String?,
    )
}