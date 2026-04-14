package com.blogzip.batch.blog

import com.blogzip.crawler.service.CrawlerHttpClient
import com.blogzip.crawler.service.RssFeedFetcher
import com.blogzip.crawler.service.WebScrapper
import com.blogzip.domain.Blog
import com.blogzip.domain.Blog.RssStatus.NO_RSS
import com.blogzip.domain.Blog.RssStatus.WITHOUT_CONTENT
import com.blogzip.domain.Blog.RssStatus.WITH_CONTENT
import com.blogzip.logger
import com.blogzip.service.ArticleQueryService
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
  private val articleQueryService: ArticleQueryService,
  private val crawlerHttpClient: CrawlerHttpClient,
  private val rssFeedFetcher: RssFeedFetcher,
  private val webScrapper: WebScrapper,
  private val slackSender: SlackSender,
) : Tasklet {

  private val log = logger()

  override fun execute(
    contribution: StepContribution,
    chunkContext: ChunkContext
  ): RepeatStatus {
    val blogs = blogService.findAll().shuffled() // 동일 시간 요청에 의한 IP 차단 방지
    val failures = mutableListOf<RefreshFailure>()
    val changedBlogs = mutableListOf<BlogChanges>()
    val operatorActions = mutableListOf<OperatorAction>()
    var processedCount = 0
    var skippedWithoutIdCount = 0

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
          ?: throw IllegalStateException("crawler metadata 응답이 null입니다. url=${blog.url}")
        val rssResolution = resolveRss(metadata.rss, blog.url)
        val cssSelectorResolution = resolveCssSelector(
          blog = blog,
          blogId = blogId,
          targetRssStatus = rssResolution.rssStatus,
        )

        val target = TargetMetadata(
          name = metadata.title,
          image = metadata.imageUrl,
          rss = rssResolution.rss,
          rssStatus = rssResolution.rssStatus,
          urlCssSelector = cssSelectorResolution.urlCssSelector,
        )

        blogService.updateMetadata(
          blogId = blogId,
          name = target.name,
          image = target.image,
          rss = target.rss,
          rssStatus = target.rssStatus,
        )
        if (blog.urlCssSelector != target.urlCssSelector) {
          blogService.updateCssSelector(blogId, target.urlCssSelector)
        }

        val fieldChanges = createFieldChanges(blog, target)
        if (fieldChanges.isNotEmpty()) {
          changedBlogs += BlogChanges(
            blogId = blogId,
            blogUrl = blog.url,
            fieldChanges = fieldChanges,
          )
        }
        cssSelectorResolution.operatorAction?.let { operatorActions += it }
        processedCount++
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
      processedCount = processedCount,
      skippedWithoutIdCount = skippedWithoutIdCount,
      changedBlogs = changedBlogs,
      operatorActions = operatorActions,
      failures = failures,
    )

    return RepeatStatus.FINISHED
  }

  private fun resolveRss(rssCandidate: String?, blogUrl: String): RssResolution {
    if (rssCandidate.isNullOrBlank()) {
      return RssResolution(
        rss = null,
        rssStatus = NO_RSS,
      )
    }
    val rss = rssCandidate.trim()

    return runCatching {
      rssFeedFetcher.getArticles(rss)
    }.fold(
      onSuccess = { articles ->
        if (articles.any { !it.content.isNullOrBlank() }) {
          RssResolution(
            rss = rss,
            rssStatus = WITH_CONTENT,
          )
        } else {
          RssResolution(
            rss = rss,
            rssStatus = WITHOUT_CONTENT,
          )
        }
      },
      onFailure = { throwable ->
        log.warn("rss 접속 실패로 NO_RSS 처리. blogUrl=$blogUrl, rss=$rss, detail=${throwable.message}")
        RssResolution(
          rss = null,
          rssStatus = NO_RSS,
        )
      }
    )
  }

  private fun resolveCssSelector(
    blog: Blog,
    blogId: Long,
    targetRssStatus: Blog.RssStatus,
  ): CssSelectorResolution {
    if (targetRssStatus != NO_RSS) {
      return CssSelectorResolution(
        urlCssSelector = blog.urlCssSelector,
        operatorAction = null,
      )
    }

    val currentCssSelector = blog.urlCssSelector?.trim()?.takeIf { it.isNotBlank() }
      ?: return CssSelectorResolution(
        urlCssSelector = null,
        operatorAction = OperatorAction(
          blogId = blogId,
          blogUrl = blog.url,
          action = "url_css_selector 직접 지정 필요",
          detail = "NO_RSS 상태이며 url_css_selector가 비어 있습니다.",
        ),
      )

    val knownArticleUrls = articleQueryService.findAllByBlogId(blogId)
      .map { it.url }
      .toSet()
    val scrapeResult = webScrapper.getArticles(
      blogUrl = blog.url,
      cssSelector = currentCssSelector,
      articleUrls = knownArticleUrls,
    )

    if (isCssSelectorInvalidOrChanged(scrapeResult, currentCssSelector)) {
      return CssSelectorResolution(
        urlCssSelector = null,
        operatorAction = OperatorAction(
          blogId = blogId,
          blogUrl = blog.url,
          action = "url_css_selector 직접 지정 필요",
          detail = "기존 url_css_selector가 더이상 유효하지 않습니다. ${scrapeResult.failCause?.message ?: ""}".trim(),
        ),
      )
    }

    return CssSelectorResolution(
      urlCssSelector = blog.urlCssSelector,
      operatorAction = null,
    )
  }

  private fun isCssSelectorInvalidOrChanged(
    scrapeResult: WebScrapper.ScrapResult,
    cssSelector: String
  ): Boolean {
    val message = scrapeResult.failCause?.message?.lowercase() ?: return false
    val selectorText = cssSelector.lowercase()

    if (!message.contains("selector")) {
      return false
    }

    return message.contains("waiting for selector")
      || message.contains("valid selector")
      || message.contains(selectorText)
  }

  private fun createFieldChanges(
    before: Blog,
    target: TargetMetadata,
  ): List<FieldChange> {
    val fieldChanges = mutableListOf<FieldChange>()
    if (before.name != target.name) {
      fieldChanges += FieldChange(
        field = "name",
        asis = before.name,
        tobe = target.name,
      )
    }
    if (before.image != target.image) {
      fieldChanges += FieldChange(
        field = "image",
        asis = before.image,
        tobe = target.image,
      )
    }
    if (before.rss != target.rss) {
      fieldChanges += FieldChange(
        field = "rss",
        asis = before.rss,
        tobe = target.rss,
      )
    }
    if (before.rssStatus != target.rssStatus) {
      fieldChanges += FieldChange(
        field = "rssStatus",
        asis = before.rssStatus.name,
        tobe = target.rssStatus.name,
      )
    }
    if (before.urlCssSelector != target.urlCssSelector) {
      fieldChanges += FieldChange(
        field = "url_css_selector",
        asis = before.urlCssSelector,
        tobe = target.urlCssSelector,
      )
    }
    return fieldChanges
  }

  private fun sendSummary(
    totalCount: Int,
    processedCount: Int,
    skippedWithoutIdCount: Int,
    changedBlogs: List<BlogChanges>,
    operatorActions: List<OperatorAction>,
    failures: List<RefreshFailure>,
  ) {
    val changeLines = changedBlogs
      .flatMap { blogChange ->
        blogChange.fieldChanges.map { fieldChange ->
          "- blogId=${blogChange.blogId}, url=${blogChange.blogUrl}, ${fieldChange.field}: ${toDisplayValue(fieldChange.asis)} -> ${toDisplayValue(fieldChange.tobe)}"
        }
      }
      .sorted()

    val operatorActionLines = operatorActions
      .distinct()
      .map {
        "- blogId=${it.blogId}, url=${it.blogUrl}, action=${it.action}${if (it.detail.isNullOrBlank()) "" else ", detail=${it.detail}"}"
      }
      .sorted()

    val failureLines = failures
      .map {
        "- blogId=${it.blogId ?: "unknown"}, url=${it.blogUrl}, action=메타데이터 갱신 실패${
          if (it.detail.isNullOrBlank()) "" else ", detail=${it.detail}"
        }"
      }
      .distinct()
      .sorted()

    val message = buildString {
      append("[refresh-blog-metadata] 블로그 메타데이터 갱신 결과\n")
      append("처리 요약: total=${totalCount}건, processed=${processedCount}건, changedBlogs=${changedBlogs.size}건, operatorAction=${operatorActionLines.size}건, failed=${failures.size}건, skippedWithoutId=${skippedWithoutIdCount}건")
      append("\n[변경된 값 (asis -> tobe)]\n")
      if (changeLines.isEmpty()) {
        append("- 없음")
      } else {
        append(changeLines.joinToString("\n"))
      }
      append("\n[운영자 대처 필요]\n")
      if (operatorActionLines.isEmpty()) {
        append("- 없음")
      } else {
        append(operatorActionLines.joinToString("\n"))
      }
      if (failureLines.isNotEmpty()) {
        append("\n[실패 내역]\n")
        append(failureLines.joinToString("\n"))
      }
    }

    log.info(message)
    slackSender.sendMessageAsync(MONITORING, message)
  }

  private fun toDisplayValue(value: String?): String {
    return value ?: "null"
  }

  private data class TargetMetadata(
    val name: String,
    val image: String?,
    val rss: String?,
    val rssStatus: Blog.RssStatus,
    val urlCssSelector: String?,
  )

  private data class RssResolution(
    val rss: String?,
    val rssStatus: Blog.RssStatus,
  )

  private data class CssSelectorResolution(
    val urlCssSelector: String?,
    val operatorAction: OperatorAction?,
  )

  private data class BlogChanges(
    val blogId: Long,
    val blogUrl: String,
    val fieldChanges: List<FieldChange>,
  )

  private data class FieldChange(
    val field: String,
    val asis: String?,
    val tobe: String?,
  )

  private data class OperatorAction(
    val blogId: Long,
    val blogUrl: String,
    val action: String,
    val detail: String?,
  )

  private data class RefreshFailure(
    val blogId: Long?,
    val blogUrl: String,
    val detail: String?,
  )
}
