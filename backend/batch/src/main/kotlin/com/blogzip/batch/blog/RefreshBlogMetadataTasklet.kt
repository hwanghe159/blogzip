package com.blogzip.batch.blog

import com.blogzip.crawler.service.CrawlerHttpClient
import com.blogzip.crawler.service.RssFeedFetcher
import com.blogzip.domain.Blog
import com.blogzip.logger
import com.blogzip.service.BlogService
import com.blogzip.slack.SlackSender
import com.blogzip.slack.SlackSender.SlackChannel.MONITORING
import org.springframework.batch.core.StepContribution
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.stereotype.Component

@Component
class RefreshBlogMetadataTasklet(
  private val blogService: BlogService,
  private val crawlerHttpClient: CrawlerHttpClient,
  private val rssFeedFetcher: RssFeedFetcher,
  private val slackSender: SlackSender,
) : Tasklet {

  private val log = logger()

  override fun execute(
    contribution: StepContribution,
    chunkContext: ChunkContext
  ): RepeatStatus {
    val blogs = blogService.findAll().shuffled() // 동일 시간 요청에 의한 IP 차단 방지
    val failures = mutableListOf<RefreshFailure>()
    var updatedCount = 0
    var skippedWithoutIdCount = 0
    var imageFallbackCount = 0
    var rssFallbackCount = 0
    var rssStatusFallbackCount = 0

    for ((index, blog) in blogs.withIndex()) {
      val blogId = blog.id
      if (blogId == null) {
        log.warn("blog id가 null이라 메타데이터 갱신을 건너뜁니다. url=${blog.url}")
        skippedWithoutIdCount++
        continue
      }

      log.info("${blog.url} 메타데이터 갱신중. (${index + 1}/${blogs.size})")

      runCatching {
        val metadata = crawlerHttpClient.getMetadata(blog.url)
        val updatedRssStatus = metadata.rss?.let { rss ->
          if (rssFeedFetcher.isContentContainsInRss(rss)) {
            Blog.RssStatus.WITH_CONTENT
          } else {
            Blog.RssStatus.WITHOUT_CONTENT
          }
        }

        if (metadata.imageUrl == null) {
          imageFallbackCount++
        }
        if (metadata.rss == null) {
          rssFallbackCount++
        }
        if (updatedRssStatus == null) {
          rssStatusFallbackCount++
        }

        if (metadata.imageUrl == null || metadata.rss == null || updatedRssStatus == null) {
          log.info(
            "crawler 메타데이터 일부가 null이라 기존값을 유지합니다. " +
              "blogId=$blogId, url=${blog.url}, image=${metadata.imageUrl != null}, rss=${metadata.rss != null}, rssStatus=${updatedRssStatus != null}"
          )
        }

        blogService.updateMetadata(
          blogId = blogId,
          name = metadata.title,
          image = metadata.imageUrl,
          rss = metadata.rss,
          rssStatus = updatedRssStatus,
        )
        updatedCount++
      }.onFailure { throwable ->
        log.error("${blog.url} 메타데이터 갱신 실패", throwable)
        failures += RefreshFailure(
          blogId = blogId,
          blogUrl = blog.url,
          detail = throwable.message,
        )
      }
    }

    sendSummary(
      totalCount = blogs.size,
      updatedCount = updatedCount,
      skippedWithoutIdCount = skippedWithoutIdCount,
      imageFallbackCount = imageFallbackCount,
      rssFallbackCount = rssFallbackCount,
      rssStatusFallbackCount = rssStatusFallbackCount,
      failures = failures,
    )

    return RepeatStatus.FINISHED
  }

  private fun sendSummary(
    totalCount: Int,
    updatedCount: Int,
    skippedWithoutIdCount: Int,
    imageFallbackCount: Int,
    rssFallbackCount: Int,
    rssStatusFallbackCount: Int,
    failures: List<RefreshFailure>,
  ) {
    val failureLines = failures
      .map {
        "- url=${it.blogUrl}, blogId=${it.blogId ?: "unknown"}, reason=REFRESH_FAILED${
          if (it.detail.isNullOrBlank()) "" else ", detail=${it.detail}"
        }"
      }
      .distinct()
      .sorted()

    val message = buildString {
      append("[refresh-blog-metadata] 블로그 메타데이터 갱신 결과\n")
      append("total=$totalCount, updated=$updatedCount, failed=${failures.size}, skippedWithoutId=$skippedWithoutIdCount\n")
      append("fallback(image=$imageFallbackCount, rss=$rssFallbackCount, rssStatus=$rssStatusFallbackCount)")
      if (failureLines.isNotEmpty()) {
        append("\n")
        append(failureLines.joinToString("\n"))
      }
    }

    log.info(message)
    slackSender.sendMessageAsync(MONITORING, message)
  }

  private data class RefreshFailure(
    val blogId: Long?,
    val blogUrl: String,
    val detail: String?,
  )
}
