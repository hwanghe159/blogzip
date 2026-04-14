package com.blogzip.batch.blog

import com.blogzip.crawler.dto.BlogMetadata
import com.blogzip.crawler.service.CrawlerHttpClient
import com.blogzip.crawler.service.RssFeedFetcher
import com.blogzip.crawler.service.RssFetchErrorType
import com.blogzip.crawler.service.RssFetchException
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
  private val metadataInvalidTitleKeywords = listOf(
    "internal server error",
    "access denied",
    "the request could not be satisfied",
    "request blocked",
    "service unavailable",
    "bad gateway",
    "gateway timeout",
    "too many requests",
    "forbidden",
    "보안 위배 접근 제한 페이지",
  )

  override fun execute(
    contribution: StepContribution,
    chunkContext: ChunkContext
  ): RepeatStatus {
    val blogs = blogService.findAll().shuffled() // 동일 시간 요청에 의한 IP 차단 방지
    val failures = mutableListOf<RefreshFailure>()
    val failureEvents = mutableListOf<FailureEvent>()
    val changedBlogs = mutableListOf<BlogChanges>()
    val operatorActions = mutableListOf<OperatorAction>()
    var processedCount = 0
    var skippedWithoutIdCount = 0
    var rssFallbackSuccessCount = 0

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

        val metadataInvalidReason = getInvalidMetadataReason(metadata)
        if (metadataInvalidReason != null) {
          val detail = "metadata 검증 실패(${metadataInvalidReason}). title=${metadata.title}"
          log.warn("metadata 품질 검증 실패로 기존값 유지. blogId=$blogId, url=${blog.url}, detail=$detail")
          operatorActions += OperatorAction(
            blogId = blogId,
            blogUrl = blog.url,
            action = "metadata 수동 확인 필요",
            detail = detail,
          )
          failureEvents += FailureEvent(
            failureType = FailureType.METADATA_QUALITY_VALIDATION,
            blogId = blogId,
            blogUrl = blog.url,
            detail = detail,
          )
          processedCount++
          return@runCatching
        }

        val rssResolution = resolveRss(
          rssCandidate = metadata.rss,
          blog = blog,
          blogId = blogId,
        )
        rssResolution.failureEvent?.let { failureEvents += it }
        rssResolution.operatorAction?.let { operatorActions += it }
        if (rssResolution.usedFallback) {
          rssFallbackSuccessCount++
        }

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

        blogService.updateMetadataPerBlog(
          blogId = blogId,
          name = target.name,
          image = target.image,
          rss = target.rss,
          rssStatus = target.rssStatus,
          shouldUpdateCssSelector = blog.urlCssSelector != target.urlCssSelector,
          urlCssSelector = target.urlCssSelector,
        )

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
        val failureType = classifyFailureType(throwable)
        log.error("${blog.url} 메타데이터 갱신 실패", throwable)
        failures += RefreshFailure(
          blogId = blogId,
          blogUrl = blog.url,
          failureType = failureType,
          detail = throwable.message,
        )
        failureEvents += FailureEvent(
          failureType = failureType,
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
      failureEvents = failureEvents,
      rssFallbackSuccessCount = rssFallbackSuccessCount,
    )

    return RepeatStatus.FINISHED
  }

  private fun resolveRss(
    rssCandidate: String?,
    blog: Blog,
    blogId: Long,
  ): RssResolution {
    if (rssCandidate.isNullOrBlank()) {
      return RssResolution(
        rss = null,
        rssStatus = NO_RSS,
        operatorAction = null,
        failureEvent = null,
        usedFallback = false,
      )
    }
    val rss = rssCandidate.trim()

    return runCatching {
      rssFeedFetcher.getArticlesWithDiagnostics(rss)
    }.fold(
      onSuccess = { result ->
        if (result.articles.any { !it.content.isNullOrBlank() }) {
          RssResolution(
            rss = rss,
            rssStatus = WITH_CONTENT,
            operatorAction = null,
            failureEvent = null,
            usedFallback = result.diagnostics.usedCurlFallback,
          )
        } else {
          RssResolution(
            rss = rss,
            rssStatus = WITHOUT_CONTENT,
            operatorAction = null,
            failureEvent = null,
            usedFallback = result.diagnostics.usedCurlFallback,
          )
        }
      },
      onFailure = { throwable ->
        val failureType = classifyFailureType(throwable)
        val failureEvent = FailureEvent(
          failureType = failureType,
          blogId = blogId,
          blogUrl = blog.url,
          detail = throwable.message,
        )

        if (shouldKeepExistingRssOnFailure(failureType, blog)) {
          log.warn("rss 접속 실패로 기존값 유지. blogUrl=${blog.url}, rss=$rss, type=${failureType.label}, detail=${throwable.message}")
          return@fold RssResolution(
            rss = blog.rss,
            rssStatus = blog.rssStatus,
            operatorAction = OperatorAction(
              blogId = blogId,
              blogUrl = blog.url,
              action = "rss 접근 실패로 기존값 유지",
              detail = throwable.message,
            ),
            failureEvent = failureEvent,
            usedFallback = false,
          )
        }

        log.warn("rss 접속 실패로 NO_RSS 처리. blogUrl=${blog.url}, rss=$rss, type=${failureType.label}, detail=${throwable.message}")
        RssResolution(
          rss = null,
          rssStatus = NO_RSS,
          operatorAction = null,
          failureEvent = failureEvent,
          usedFallback = false,
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
    failureEvents: List<FailureEvent>,
    rssFallbackSuccessCount: Int,
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

    val failureTypeSummaryLines = failureEvents
      .groupBy { it.failureType.label }
      .mapValues { (_, events) -> events.size }
      .entries
      .sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key })
      .map { "- ${it.key}: ${it.value}건" }

    val failureLines = failures
      .map {
        "- blogId=${it.blogId ?: "unknown"}, url=${it.blogUrl}, type=${it.failureType.label}, action=메타데이터 갱신 실패${
          if (it.detail.isNullOrBlank()) "" else ", detail=${it.detail}"
        }"
      }
      .distinct()
      .sorted()

    val message = buildString {
      append("[refresh-blog-metadata] 블로그 메타데이터 갱신 결과\n")
      append("처리 요약: total=${totalCount}건, processed=${processedCount}건, changedBlogs=${changedBlogs.size}건, operatorAction=${operatorActionLines.size}건, failed=${failures.size}건, skippedWithoutId=${skippedWithoutIdCount}건")
      append("\n[실패 유형 요약]\n")
      if (failureTypeSummaryLines.isEmpty()) {
        append("- 없음")
      } else {
        append(failureTypeSummaryLines.joinToString("\n"))
      }
      append("\n[RSS fallback 성공]\n")
      append("- ${rssFallbackSuccessCount}건")
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

  private fun getInvalidMetadataReason(metadata: BlogMetadata): String? {
    val title = metadata.title.trim()
    if (title.isBlank()) {
      return "title 비어있음"
    }
    val normalizedTitle = title.lowercase()
    if (normalizedTitle.startsWith("error:")) {
      return "title error 접두어"
    }
    val matchedKeyword = metadataInvalidTitleKeywords.firstOrNull { normalizedTitle.contains(it) }
    if (matchedKeyword != null) {
      return "에러 title 키워드 감지($matchedKeyword)"
    }
    return null
  }

  private fun shouldKeepExistingRssOnFailure(failureType: FailureType, blog: Blog): Boolean {
    if (blog.rss.isNullOrBlank()) {
      return false
    }
    return failureType == FailureType.RSS_BLOCKED ||
      failureType == FailureType.RSS_NOT_XML ||
      failureType == FailureType.NETWORK_OR_TIMEOUT
  }

  private fun classifyFailureType(throwable: Throwable): FailureType {
    if (throwable is RssFetchException) {
      return when (throwable.type) {
        RssFetchErrorType.BLOCKED -> FailureType.RSS_BLOCKED
        RssFetchErrorType.NOT_XML_RESPONSE -> FailureType.RSS_NOT_XML
        RssFetchErrorType.INVALID_XML -> FailureType.RSS_XML_PARSE
        RssFetchErrorType.NETWORK,
        RssFetchErrorType.TIMEOUT,
        RssFetchErrorType.CURL_FAILED -> FailureType.NETWORK_OR_TIMEOUT
        RssFetchErrorType.UNKNOWN -> FailureType.ETC
      }
    }

    val message = throwable.message.orEmpty()
    return when {
      message.startsWith("RSS_FETCH_BLOCKED:") -> FailureType.RSS_BLOCKED
      message.startsWith("RSS_NOT_XML_RESPONSE:") -> FailureType.RSS_NOT_XML
      message.startsWith("RSS_INVALID_XML:") -> FailureType.RSS_XML_PARSE
      message.contains("timeout", ignoreCase = true) ||
        message.contains("timed out", ignoreCase = true) ||
        message.contains("connection reset", ignoreCase = true) ||
        message.contains("refused", ignoreCase = true) ||
        message.contains("unknown host", ignoreCase = true) -> FailureType.NETWORK_OR_TIMEOUT
      else -> FailureType.ETC
    }
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
    val operatorAction: OperatorAction?,
    val failureEvent: FailureEvent?,
    val usedFallback: Boolean,
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
    val failureType: FailureType,
    val detail: String?,
  )

  private data class FailureEvent(
    val failureType: FailureType,
    val blogId: Long?,
    val blogUrl: String,
    val detail: String?,
  )

  private enum class FailureType(val label: String) {
    RSS_BLOCKED("RSS 접근 차단"),
    RSS_NOT_XML("RSS 응답 형식 오류"),
    RSS_XML_PARSE("RSS XML 파싱 오류"),
    METADATA_QUALITY_VALIDATION("metadata 품질 검증 실패"),
    NETWORK_OR_TIMEOUT("네트워크/타임아웃"),
    ETC("기타 오류"),
  }
}
