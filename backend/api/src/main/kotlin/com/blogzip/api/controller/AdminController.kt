package com.blogzip.api.controller

import com.blogzip.ai.summary.ArticleContentSequentialSummarizer
import com.blogzip.ai.summary.ArticleToSummarize
import com.blogzip.ai.summary.SummarizedArticleResult
import com.blogzip.api.admin.AdminRequired
import com.blogzip.api.dto.PaginationResponse
import com.blogzip.api.dto.admin.*
import com.blogzip.common.DomainException
import com.blogzip.common.ErrorCode
import com.blogzip.crawler.service.CrawlerHttpClient
import com.blogzip.domain.ArticleReport
import com.blogzip.domain.BlogUrl
import com.blogzip.service.ArticleCommandService
import com.blogzip.service.ArticleCreatedDateUpdateCommand
import com.blogzip.service.ArticleQueryService
import com.blogzip.service.ArticleReportService
import com.blogzip.service.BlogService
import com.blogzip.service.KeywordService
import jakarta.validation.Valid
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.URI
import java.net.URISyntaxException
import java.util.Locale

@RestController
class AdminController(
  private val keywordService: KeywordService,
  private val articleCommandService: ArticleCommandService,
  private val articleQueryService: ArticleQueryService,
  private val articleReportService: ArticleReportService,
  private val articleContentSequentialSummarizer: ArticleContentSequentialSummarizer,
  private val blogService: BlogService,
  private val crawlerHttpClient: CrawlerHttpClient,
) {

  @AdminRequired
  @GetMapping("/api/admin/keyword/overview")
  fun getKeywordOverview(): ResponseEntity<KeywordOverviewResponse> {
    val keywordOverview = keywordService.getOverview()
    return ResponseEntity.ok(KeywordOverviewResponse.from(keywordOverview))
  }

  @AdminRequired
  @PatchMapping("/api/admin/keyword/{value}")
  fun updateKeyword(
    @PathVariable value: String,
    @RequestBody request: KeywordUpdateRequest,
  ): ResponseEntity<KeywordOverviewResponse> {
    keywordService.update(value, request.value, request.isVisible)
    val keywordOverview = keywordService.getOverview()
    return ResponseEntity.ok(KeywordOverviewResponse.from(keywordOverview))
  }

  @AdminRequired
  @PatchMapping("/api/admin/keyword/id/{keywordId}")
  fun updateKeywordById(
    @PathVariable keywordId: Long,
    @RequestBody request: KeywordUpdateRequest,
  ): ResponseEntity<KeywordOverviewResponse> {
    keywordService.updateById(keywordId, request.value, request.isVisible)
    val keywordOverview = keywordService.getOverview()
    return ResponseEntity.ok(KeywordOverviewResponse.from(keywordOverview))
  }

  @AdminRequired
  @PostMapping("/api/admin/keyword")
  fun createKeyword(
    @Valid @RequestBody request: KeywordCreateRequest,
  ): ResponseEntity<KeywordCreateResponse> {
    val created = keywordService.createHeadKeyword(
      rawValue = request.value,
      isVisible = request.isVisible,
    )
    return ResponseEntity.ok(KeywordCreateResponse.from(created))
  }

  @AdminRequired
  @PostMapping("/api/admin/keyword/merge")
  fun mergeKeywords(
    @RequestParam(required = true) src: String,
    @RequestParam(required = true) dest: String,
  ): ResponseEntity<KeywordOverviewResponse> {
    keywordService.merge(src, dest)
    val keywordOverview = keywordService.getOverview()
    return ResponseEntity.ok(KeywordOverviewResponse.from(keywordOverview))
  }

  @AdminRequired
  @PostMapping("/api/admin/keyword/merge/by-id")
  fun mergeKeywordsById(
    @RequestBody request: KeywordMergeByIdRequest,
  ): ResponseEntity<KeywordOverviewResponse> {
    keywordService.mergeById(request.srcKeywordId, request.destKeywordId)
    val keywordOverview = keywordService.getOverview()
    return ResponseEntity.ok(KeywordOverviewResponse.from(keywordOverview))
  }

  @AdminRequired
  @PatchMapping("/api/admin/keyword/{keywordId}/head")
  fun updateKeywordHead(
    @PathVariable keywordId: Long,
    @RequestBody request: KeywordHeadUpdateRequest,
  ): ResponseEntity<KeywordHeadUpdateResponse> {
    val result = keywordService.updateHead(keywordId, request.headKeywordId)
    return ResponseEntity.ok(KeywordHeadUpdateResponse.from(result))
  }

  @AdminRequired
  @GetMapping("/api/admin/article/recent")
  fun getRecentArticles(
    @RequestParam(required = false) next: Long?,
    @RequestParam(required = false, defaultValue = "20") size: Int,
  ): ResponseEntity<PaginationResponse<AdminRecentArticleResponse>> {
    val recentArticles = articleQueryService.searchRecentForAdmin(next, size)
    val reportCounts = articleReportService.getCountsByArticleIds(
      recentArticles.items.mapNotNull { it.article.id }
    )
    return ResponseEntity.ok(
      PaginationResponse(
        items = recentArticles.items.map { item ->
          AdminRecentArticleResponse.from(
            article = item.article,
            blog = item.blog,
            appliedSummary = item.appliedSummary,
            reportCount = item.article.id?.let { reportCounts[it] },
          )
        },
        next = recentArticles.next,
      )
    )
  }

  @AdminRequired
  @GetMapping("/api/admin/article/{articleId}/report")
  fun getArticleReports(
    @PathVariable articleId: Long,
  ): ResponseEntity<List<AdminArticleReportResponse>> {
    val article = articleQueryService.findById(articleId)
    val blog = blogService.findById(article.blogId)
    val reports = articleReportService.getAllByArticleId(articleId)
      .map { report ->
        AdminArticleReportResponse.from(
          report = report,
          article = article,
          blog = blog,
        )
      }
    return ResponseEntity.ok(reports)
  }

  @AdminRequired
  @GetMapping("/api/admin/report/received")
  fun getReceivedReports(
    @RequestParam(required = false) next: Long?,
    @RequestParam(required = false, defaultValue = "20") size: Int,
  ): ResponseEntity<PaginationResponse<AdminArticleReportResponse>> {
    val pagedReports = articleReportService.getByStatus(
      status = ArticleReport.Status.RECEIVED,
      next = next,
      size = size,
    )
    val articlesById = articleQueryService.findAllById(pagedReports.items.map { it.articleId }.distinct())
      .associateBy { it.id!! }
    val blogsById = blogService.findAllByIds(articlesById.values.map { it.blogId }.distinct())
      .associateBy { it.id!! }

    return ResponseEntity.ok(
      PaginationResponse(
        items = pagedReports.items.mapNotNull { report ->
          val article = articlesById[report.articleId] ?: return@mapNotNull null
          val blog = blogsById[article.blogId] ?: return@mapNotNull null
          AdminArticleReportResponse.from(
            report = report,
            article = article,
            blog = blog,
          )
        },
        next = pagedReports.next,
      )
    )
  }

  @AdminRequired
  @PatchMapping("/api/admin/report/{reportId}/status")
  fun updateArticleReportStatus(
    @PathVariable reportId: Long,
    @RequestBody request: AdminArticleReportStatusUpdateRequest,
  ): ResponseEntity<AdminArticleReportResponse> {
    val updatedReport = articleReportService.updateStatus(
      reportId = reportId,
      status = request.status,
    )
    val article = articleQueryService.findById(updatedReport.articleId)
    val blog = blogService.findById(article.blogId)
    return ResponseEntity.ok(
      AdminArticleReportResponse.from(
        report = updatedReport,
        article = article,
        blog = blog,
      )
    )
  }

  @AdminRequired
  @PatchMapping("/api/admin/article/{articleId}/created-date")
  fun updateArticleCreatedDate(
    @PathVariable articleId: Long,
    @RequestBody request: ArticleCreatedDateUpdateRequest,
  ): ResponseEntity<ArticleCreatedDateUpdateResponse> {
    val result = articleCommandService.updateCreatedDate(articleId, request.createdDate)
    return ResponseEntity.ok(ArticleCreatedDateUpdateResponse.from(result))
  }

  @AdminRequired
  @PatchMapping("/api/admin/article/created-date")
  fun updateArticleCreatedDates(
    @RequestBody request: ArticleCreatedDateBulkUpdateRequest,
  ): ResponseEntity<List<ArticleCreatedDateUpdateResponse>> {
    val commands = request.items.map {
      ArticleCreatedDateUpdateCommand(
        articleId = it.articleId,
        createdDate = it.createdDate,
      )
    }
    val updatedResults = articleCommandService.updateCreatedDates(commands)
      .map { ArticleCreatedDateUpdateResponse.from(it) }
    return ResponseEntity.ok(updatedResults)
  }

  @AdminRequired
  @GetMapping("/api/admin/article/{articleId}/summary")
  fun getArticleSummaries(
    @PathVariable articleId: Long,
  ): ResponseEntity<List<ArticleSummaryResponse>> {
    val summaries = articleCommandService.getSummaries(articleId)
      .map { ArticleSummaryResponse.from(it) }
    return ResponseEntity.ok(summaries)
  }

  @AdminRequired
  @PatchMapping("/api/admin/article/summary/{summaryId}/apply")
  fun applyArticleSummary(
    @PathVariable summaryId: Long,
  ): ResponseEntity<ArticleSummaryResponse> {
    val appliedSummary = articleCommandService.applySummary(summaryId)
    return ResponseEntity.ok(ArticleSummaryResponse.from(appliedSummary))
  }

  @AdminRequired
  @PostMapping("/api/admin/article/re-summary/preview")
  fun previewReSummaryArticles(
    @RequestBody request: ArticleResummaryPreviewRequest,
  ): ResponseEntity<ArticleResummaryPreviewResponse> {
    val articleIds = request.articleIds.distinct()
    if (articleIds.isEmpty()) {
      return ResponseEntity.ok(ArticleResummaryPreviewResponse(items = emptyList()))
    }
    val articlesById = articleQueryService.findAllById(articleIds)
      .associateBy { it.id!! }
    val appliedSummaries = articleCommandService.getAppliedSummaries(articleIds)
    val summarizeResultsByArticleId = articleContentSequentialSummarizer
      .summarizeAndGetKeywordsAll(
        articleIds.mapNotNull { articleId ->
          articlesById[articleId]
            ?.let { article -> ArticleToSummarize(id = article.id!!, content = article.content) }
        }
      )
      .associateBy {
        when (it) {
          is SummarizedArticleResult.Success -> it.article.id
          is SummarizedArticleResult.Failure -> it.articleId
        }
      }
    val responseItems = articleIds.map { articleId ->
      val article = articlesById[articleId]
      val currentSummary = appliedSummaries[articleId]?.summary
      val currentSummarizedBy = appliedSummaries[articleId]?.summarizedBy
      if (article == null) {
        return@map ArticleResummaryPreviewItemResponse.fromFailure(
          articleId = articleId,
          article = null,
          currentSummary = currentSummary,
          currentSummarizedBy = currentSummarizedBy,
          errorMessage = ErrorCode.ARTICLE_NOT_FOUND.message,
        )
      }
      when (val summarizeResult = summarizeResultsByArticleId[articleId]) {
        is SummarizedArticleResult.Success -> runCatching {
          val candidateSummary = articleCommandService.createSummaryCandidate(
            articleId = article.id!!,
            summary = summarizeResult.article.summary,
            summarizedBy = summarizeResult.article.summarizedBy,
          )
          ArticleResummaryPreviewItemResponse.fromSuccess(
            article = article,
            currentSummary = currentSummary,
            currentSummarizedBy = currentSummarizedBy,
            result = summarizeResult,
            candidateSummaryId = candidateSummary.id!!,
          )
        }.getOrElse { throwable ->
          ArticleResummaryPreviewItemResponse.fromFailure(
            articleId = articleId,
            article = article,
            currentSummary = currentSummary,
            currentSummarizedBy = currentSummarizedBy,
            errorMessage = throwable.message ?: "재요약 후보 저장에 실패했습니다.",
          )
        }

        is SummarizedArticleResult.Failure -> ArticleResummaryPreviewItemResponse.fromFailure(
          articleId = articleId,
          article = article,
          currentSummary = currentSummary,
          currentSummarizedBy = currentSummarizedBy,
          errorMessage = summarizeResult.throwable.message
            ?: "재요약에 실패했습니다.",
        )

        null -> ArticleResummaryPreviewItemResponse.fromFailure(
          articleId = articleId,
          article = article,
          currentSummary = currentSummary,
          currentSummarizedBy = currentSummarizedBy,
          errorMessage = "재요약 결과를 찾지 못했습니다.",
        )
      }
    }
    return ResponseEntity.ok(
      ArticleResummaryPreviewResponse(
        items = responseItems,
      )
    )
  }

  @AdminRequired
  @PostMapping("/api/admin/article/re-summary/apply")
  fun applyReSummaryArticles(
    @Valid @RequestBody request: ArticleResummaryApplyRequest,
  ): ResponseEntity<ArticleResummaryApplyResponse> {
    if (request.items.isEmpty()) {
      return ResponseEntity.ok(ArticleResummaryApplyResponse(items = emptyList()))
    }
    val responseItems = request.items.map { item ->
      runCatching {
        val appliedSummary = articleCommandService.applySummary(item.articleSummaryId)
        if (item.applyKeywords && item.keywords.isNotEmpty()) {
          val normalizedKeywords = item.keywords
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
          if (normalizedKeywords.isNotEmpty()) {
            keywordService.addArticleKeywords(appliedSummary.articleId, normalizedKeywords)
          }
        }
        appliedSummary
      }.fold(
        onSuccess = { appliedSummary ->
          ArticleResummaryApplyItemResponse(
            articleSummaryId = item.articleSummaryId,
            articleId = appliedSummary.articleId,
            applied = true,
            summaryApplied = true,
            keywordsApplied = item.applyKeywords && item.keywords.isNotEmpty(),
            errorMessage = null,
          )
        },
        onFailure = { throwable ->
          ArticleResummaryApplyItemResponse(
            articleSummaryId = item.articleSummaryId,
            articleId = null,
            applied = false,
            summaryApplied = false,
            keywordsApplied = false,
            errorMessage = throwable.message ?: "요약 결과 적용에 실패했습니다.",
          )
        }
      )
    }
    return ResponseEntity.ok(
      ArticleResummaryApplyResponse(
        items = responseItems,
      )
    )
  }

  @AdminRequired
  @PostMapping("/api/admin/crawler/css-selector/test")
  fun testCssSelector(
    @RequestBody request: CssSelectorTestRequest,
  ): ResponseEntity<CssSelectorTestResponse> {
    val normalizedBlogUrl = normalizeBlogUrl(request.blogUrl)
    val document = getDocument(normalizedBlogUrl)
      ?: return ResponseEntity.ok(
        failedCssSelectorTestResponse(
          blogUrl = normalizedBlogUrl,
          cssSelector = request.cssSelector,
          message = "블로그 페이지 HTML을 가져오지 못했습니다.",
        )
      )
    return ResponseEntity.ok(
      buildCssSelectorTestResponse(
        document = document,
        blogUrl = normalizedBlogUrl,
        cssSelector = request.cssSelector,
        sampleSize = request.sampleSize,
      )
    )
  }

  @AdminRequired
  @PostMapping("/api/admin/crawler/css-selector/suggest")
  fun suggestCssSelector(
    @RequestBody request: CssSelectorSuggestRequest,
  ): ResponseEntity<CssSelectorSuggestResponse> {
    val normalizedBlogUrl = normalizeBlogUrl(request.blogUrl)
    val document = getDocument(normalizedBlogUrl)
      ?: return ResponseEntity.ok(
        CssSelectorSuggestResponse(
          success = false,
          blogUrl = normalizedBlogUrl,
          candidates = emptyList(),
          message = "블로그 페이지 HTML을 가져오지 못했습니다.",
        )
      )
    val candidateLimit = request.candidateLimit.coerceIn(1, 20)
    val sampleSize = request.sampleSize.coerceIn(1, 20)
    val expectedFirstTitle = normalizeHintText(request.firstArticleTitle)
    val expectedFirstUrl = normalizeComparableUrl(request.firstArticleUrl)
    val hasHint = expectedFirstTitle != null || expectedFirstUrl != null
    val candidates = buildCssSelectorCandidates(document)
      .mapNotNull { selector ->
        val extracted = extractSelectorMatches(document, normalizedBlogUrl, selector)
        if (extracted.errorMessage != null || extracted.matches.isEmpty()) {
          return@mapNotNull null
        }
        val internalUrlCount = countInternalUrls(normalizedBlogUrl, extracted.matches)
        val baseConfidence = calculateConfidence(
          blogUrl = normalizedBlogUrl,
          matchedElementCount = extracted.matchedElementCount,
          matches = extracted.matches,
        )
        val hintMatch = calculateHintMatch(
          matches = extracted.matches,
          expectedTitle = expectedFirstTitle,
          expectedUrl = expectedFirstUrl,
        )
        CssSelectorCandidateResponse(
          selector = selector,
          confidence = calculateCombinedConfidence(
            baseConfidence = baseConfidence,
            hintScore = hintMatch.score,
            hasExpectedTitle = expectedFirstTitle != null,
            hasExpectedUrl = expectedFirstUrl != null,
            titleMatched = hintMatch.titleMatched,
            urlMatched = hintMatch.urlMatched,
          ),
          baseConfidence = baseConfidence,
          hintScore = hintMatch.score,
          firstArticleTitleMatched = hintMatch.titleMatched,
          firstArticleUrlMatched = hintMatch.urlMatched,
          matchedElementCount = extracted.matchedElementCount,
          extractableUrlCount = extracted.matches.size,
          internalUrlCount = internalUrlCount,
          sampleMatches = extracted.matches.take(sampleSize),
        )
      }
      .filter { it.extractableUrlCount >= 2 }
    val hintedCandidates = if (hasHint) {
      candidates
        .filter { candidate ->
          candidate.firstArticleTitleMatched || candidate.firstArticleUrlMatched || candidate.hintScore >= 0.35
        }
    } else {
      candidates
    }
    val finalCandidates = (if (hintedCandidates.isNotEmpty()) hintedCandidates else candidates)
      .sortedWith(
        compareByDescending<CssSelectorCandidateResponse> { it.confidence }
          .thenByDescending { it.hintScore }
          .thenByDescending { it.extractableUrlCount }
          .thenByDescending { it.internalUrlCount }
          .thenBy { it.selector.length }
      )
      .take(candidateLimit)

    return ResponseEntity.ok(
      CssSelectorSuggestResponse(
        success = finalCandidates.isNotEmpty(),
        blogUrl = normalizedBlogUrl,
        candidates = finalCandidates,
        message = when {
          finalCandidates.isEmpty() -> "추천할 selector를 찾지 못했습니다. hint 없이 다시 시도해 보세요."
          hasHint && hintedCandidates.isEmpty() -> "입력한 첫 글 제목/URL과 정확히 맞는 후보가 없어 일반 추천 결과를 보여줍니다."
          else -> null
        },
      )
    )
  }

  @AdminRequired
  @GetMapping("/api/admin/blog/requiring-selector")
  fun getBlogsRequiringSelector(): ResponseEntity<List<BlogRequiringSelectorResponse>> {
    val blogs = blogService.findBlogsRequiringCssSelector()
    return ResponseEntity.ok(
      blogs.map { blog -> BlogRequiringSelectorResponse.from(blog) }
    )
  }

  @AdminRequired
  @PostMapping("/api/admin/blog/css-selector/by-url")
  fun updateBlogCssSelectorByUrl(
    @RequestBody request: BlogCssSelectorUpdateByUrlRequest,
  ): ResponseEntity<BlogCssSelectorUpdateResponse> {
    val normalizedBlogUrl = normalizeBlogUrl(request.blogUrl)
    val blog = blogService.findByUrl(normalizedBlogUrl)
    return ResponseEntity.ok(
      buildBlogCssSelectorUpdateResponse(
        blogId = blog.id!!,
        blogUrl = blog.url,
        cssSelector = request.cssSelector,
        sampleSize = request.sampleSize,
        force = request.force,
      )
    )
  }

  private fun buildBlogCssSelectorUpdateResponse(
    blogId: Long,
    blogUrl: String,
    cssSelector: String,
    sampleSize: Int,
    force: Boolean,
  ): BlogCssSelectorUpdateResponse {
    val trimmedCssSelector = cssSelector.trim()
    if (trimmedCssSelector.isBlank()) {
      val testResult = failedCssSelectorTestResponse(
        blogUrl = blogUrl,
        cssSelector = trimmedCssSelector,
        message = "cssSelector는 비어 있을 수 없습니다.",
      )
      return BlogCssSelectorUpdateResponse(
        blogId = blogId,
        blogUrl = blogUrl,
        cssSelector = trimmedCssSelector,
        saved = false,
        testResult = testResult,
        message = "저장하지 않았습니다.",
      )
    }
    val document = getDocument(blogUrl)
      ?: run {
        val testResult = failedCssSelectorTestResponse(
          blogUrl = blogUrl,
          cssSelector = trimmedCssSelector,
          message = "블로그 페이지 HTML을 가져오지 못했습니다.",
        )
        return BlogCssSelectorUpdateResponse(
          blogId = blogId,
          blogUrl = blogUrl,
          cssSelector = trimmedCssSelector,
          saved = false,
          testResult = testResult,
          message = "저장하지 않았습니다.",
        )
      }
    val testResult = buildCssSelectorTestResponse(
      document = document,
      blogUrl = blogUrl,
      cssSelector = trimmedCssSelector,
      sampleSize = sampleSize,
    )
    val shouldSave = testResult.success || force
    if (shouldSave) {
      blogService.updateCssSelector(blogId, trimmedCssSelector)
    }
    return BlogCssSelectorUpdateResponse(
      blogId = blogId,
      blogUrl = blogUrl,
      cssSelector = trimmedCssSelector,
      saved = shouldSave,
      testResult = testResult,
      message = when {
        shouldSave && !testResult.success -> "force=true 로 저장했습니다. selector 테스트는 실패 상태입니다."
        shouldSave -> "selector를 저장했고 스모크 테스트도 통과했습니다."
        else -> "테스트 실패로 저장하지 않았습니다. 강제 저장하려면 force=true 를 사용하세요."
      },
    )
  }

  @AdminRequired
  @PatchMapping("/api/admin/blog/{blogId}/css-selector")
  fun updateBlogCssSelectorById(
    @PathVariable blogId: Long,
    @RequestBody request: BlogCssSelectorUpdateRequest,
  ): ResponseEntity<BlogCssSelectorUpdateResponse> {
    val blog = blogService.findById(blogId)
    val cssSelector = request.cssSelector.trim()
    return ResponseEntity.ok(
      buildBlogCssSelectorUpdateResponse(
        blogId = blog.id!!,
        blogUrl = blog.url,
        cssSelector = cssSelector,
        sampleSize = request.sampleSize,
        force = request.force,
      )
    )
  }

  private fun normalizeBlogUrl(blogUrl: String): String {
    return try {
      BlogUrl.from(blogUrl).toString()
    } catch (e: URISyntaxException) {
      throw DomainException(ErrorCode.BLOG_URL_NOT_VALID)
    }
  }

  private fun getDocument(blogUrl: String): Document? {
    val html = crawlerHttpClient.getContent(blogUrl)
      ?: return null
    return Jsoup.parse(html, blogUrl)
  }

  private fun failedCssSelectorTestResponse(
    blogUrl: String,
    cssSelector: String,
    message: String,
  ): CssSelectorTestResponse {
    return CssSelectorTestResponse(
      success = false,
      blogUrl = blogUrl,
      cssSelector = cssSelector,
      matchedElementCount = 0,
      extractableUrlCount = 0,
      sampleMatches = emptyList(),
      message = message,
    )
  }

  private fun buildCssSelectorTestResponse(
    document: Document,
    blogUrl: String,
    cssSelector: String,
    sampleSize: Int,
  ): CssSelectorTestResponse {
    val extracted = extractSelectorMatches(document, blogUrl, cssSelector)
    if (extracted.errorMessage != null) {
      return failedCssSelectorTestResponse(
        blogUrl = blogUrl,
        cssSelector = cssSelector,
        message = extracted.errorMessage,
      )
    }
    return CssSelectorTestResponse(
      success = extracted.matches.isNotEmpty(),
      blogUrl = blogUrl,
      cssSelector = cssSelector,
      matchedElementCount = extracted.matchedElementCount,
      extractableUrlCount = extracted.matches.size,
      sampleMatches = extracted.matches.take(sampleSize.coerceIn(1, 20)),
      message = when {
        extracted.matchedElementCount == 0 -> "selector와 일치하는 요소를 찾지 못했습니다."
        extracted.matches.isEmpty() -> "요소는 찾았지만 링크(URL) 추출에 실패했습니다."
        else -> null
      },
    )
  }

  private data class ExtractedSelectorMatches(
    val matchedElementCount: Int,
    val matches: List<CssSelectorTestMatchResponse>,
    val errorMessage: String? = null,
  )

  private fun extractSelectorMatches(
    document: Document,
    blogUrl: String,
    cssSelector: String,
  ): ExtractedSelectorMatches {
    val elements = try {
      document.select(cssSelector)
    } catch (exception: Exception) {
      return ExtractedSelectorMatches(
        matchedElementCount = 0,
        matches = emptyList(),
        errorMessage = "CSS selector 문법 오류: ${exception.message}",
      )
    }
    val matches = elements
      .mapNotNull { element: Element ->
        val href = extractHrefFromElement(element)
        if (href.isBlank()) {
          return@mapNotNull null
        }
        val resolvedUrl = resolveUrl(blogUrl, href)
          ?: return@mapNotNull null
        val title = extractTitleFromElement(element, resolvedUrl)
        CssSelectorTestMatchResponse(
          title = title,
          url = resolvedUrl,
        )
      }
      .distinctBy { it.url }
    return ExtractedSelectorMatches(
      matchedElementCount = elements.size,
      matches = matches,
    )
  }

  private fun extractHrefFromElement(element: Element): String {
    extractUrlFromAttributes(element)
      .takeIf { it.isNotBlank() }
      ?.let { return it }

    element.closest("a[href]")
      ?.let { anchor ->
        extractUrlFromAttributes(anchor)
          .takeIf { it.isNotBlank() }
          ?.let { return it }
      }

    element.selectFirst("a[href]")
      ?.let { anchor ->
        extractUrlFromAttributes(anchor)
          .takeIf { it.isNotBlank() }
          ?.let { return it }
      }

    element.selectFirst("[data-href], [data-url], [data-link], [data-permalink], [data-post-url], [data-target-url], [href]")
      ?.let { candidate ->
        extractUrlFromAttributes(candidate)
          .takeIf { it.isNotBlank() }
          ?.let { return it }
      }
    return ""
  }

  private fun extractUrlFromAttributes(element: Element): String {
    val urlAttributes = listOf(
      "href",
      "data-href",
      "data-url",
      "data-link",
      "data-permalink",
      "data-post-url",
      "data-target-url",
    )
    return urlAttributes
      .asSequence()
      .map { attribute -> element.attr(attribute).trim() }
      .firstOrNull { value -> value.isNotBlank() }
      .orEmpty()
  }

  private fun extractTitleFromElement(
    element: Element,
    fallbackUrl: String,
  ): String {
    val candidates = buildList {
      add(normalizeWhitespace(element.ownText()))
      add(normalizeWhitespace(element.text()))
      add(normalizeWhitespace(element.attr("title")))
      add(normalizeWhitespace(element.attr("aria-label")))
      element.closest("a[href]")?.text()?.let { add(normalizeWhitespace(it)) }
      element.selectFirst("a[href]")?.text()?.let { add(normalizeWhitespace(it)) }
    }.filter { it.isNotBlank() }

    val title = candidates.firstOrNull { it.length in 8..180 }
      ?: candidates.firstOrNull()
      ?: fallbackUrl
    return title.take(220)
  }

  private fun normalizeWhitespace(value: String): String {
    return value
      .replace(Regex("\\s+"), " ")
      .trim()
  }

  private fun resolveUrl(baseUrl: String, href: String): String? {
    val trimmedHref = href.trim()
    if (trimmedHref.isBlank() || trimmedHref.startsWith("#")) {
      return null
    }
    val resolvedUri = runCatching {
      URI(baseUrl).resolve(trimmedHref).normalize()
    }.getOrNull()
      ?: return null
    val scheme = resolvedUri.scheme?.lowercase(Locale.ROOT)
      ?: return null
    if (scheme !in setOf("http", "https")) {
      return null
    }
    return resolvedUri.toString()
  }

  private fun buildCssSelectorCandidates(document: Document): List<String> {
    val candidates = linkedSetOf(
      "article a[href]",
      "main article a[href]",
      "main a[href]",
      "section a[href]",
      "h2 a[href]",
      "h3 a[href]",
      ".post a[href]",
      ".posts a[href]",
      ".article a[href]",
      ".entry a[href]",
      ".entry-title a[href]",
      ".content a[href]",
      ".list a[href]",
      ".feed a[href]",
      "a[href] > h1",
      "a[href] > h2",
      "a[href] > h3",
      "a[href] > h4",
      "a[href] > p",
      "a[href] > div",
      "article h2",
      "article h3",
      "main h2",
      "main h3",
      "section h2",
      "section h3",
      ".post h2",
      ".post h3",
      ".entry h2",
      ".entry h3",
      ".content h2",
      ".content h3",
      "[class*=title] h2",
      "[class*=title] h3",
      "[class*=tit] h2",
      "[class*=tit] h3",
    )
    val anchors = document.select("a[href]").take(300)
    for (anchor in anchors) {
      addAnchorSelectorCandidates(anchor, candidates)
    }

    val titleLikeElements = document
      .select("h1, h2, h3, h4, h5, h6, p, div, span, [class*=title], [class*=tit], [class*=subject], [class*=headline]")
      .take(400)
      .filter { element -> isLikelySelectorTargetElement(element) }
    for (element in titleLikeElements) {
      addTitleElementCandidates(element, candidates)
    }
    return candidates
      .asSequence()
      .map { it.trim() }
      .filter { it.isNotBlank() }
      .filter { it.length <= 180 }
      .take(350)
      .toList()
  }

  private fun addAnchorSelectorCandidates(
    anchor: Element,
    candidates: MutableSet<String>,
  ) {
    anchor.classNames()
      .filter { isMeaningfulCssToken(it) }
      .take(3)
      .forEach { className ->
        candidates.add("a.$className[href]")
        candidates.add(".$className a[href]")
      }
    addContainerSelectors(anchor.parent(), candidates)
    addContainerSelectors(anchor.parent()?.parent(), candidates)
    val parentTag = anchor.parent()?.tagName()
    if (parentTag in setOf("h1", "h2", "h3", "h4", "h5", "h6")) {
      candidates.add("$parentTag a[href]")
    }

    val titleNodes = anchor
      .select("h1, h2, h3, h4, h5, h6, p, div, span")
      .take(4)
      .filter { isLikelySelectorTargetElement(it) }

    for (titleNode in titleNodes) {
      val tagName = titleNode.tagName()
      candidates.add("a[href] > $tagName")
      titleNode.classNames()
        .filter { isMeaningfulCssToken(it) }
        .take(2)
        .forEach { className ->
          candidates.add("a[href] > $tagName.$className")
        }
      addTitleContainerSelectors(anchor.parent(), tagName, candidates)
      addTitleContainerSelectors(anchor.parent()?.parent(), tagName, candidates)
      buildCompactPathSelector(titleNode)
        ?.let { pathSelector -> candidates.add(pathSelector) }
    }
  }

  private fun addTitleElementCandidates(
    element: Element,
    candidates: MutableSet<String>,
  ) {
    val tagName = element.tagName()
    element.classNames()
      .filter { isMeaningfulCssToken(it) }
      .take(2)
      .forEach { className ->
        candidates.add("$tagName.$className")
        candidates.add(".$className $tagName")
      }
    val id = element.id().trim()
    if (isMeaningfulCssToken(id)) {
      candidates.add("#$id")
      candidates.add("#$id $tagName")
    }

    element.closest("a[href]")?.let { anchor ->
      candidates.add("a[href] > $tagName")
      addContainerSelectors(anchor.parent(), candidates)
      addTitleContainerSelectors(anchor.parent(), tagName, candidates)
    }
    addTitleContainerSelectors(element.parent(), tagName, candidates)
    addTitleContainerSelectors(element.parent()?.parent(), tagName, candidates)
    buildCompactPathSelector(element)
      ?.let { pathSelector -> candidates.add(pathSelector) }
  }

  private fun addTitleContainerSelectors(
    container: Element?,
    titleTagName: String,
    candidates: MutableSet<String>,
  ) {
    if (container == null) {
      return
    }
    val containerTag = container.tagName()
    container.classNames()
      .filter { isMeaningfulCssToken(it) }
      .take(3)
      .forEach { className ->
        candidates.add(".$className $titleTagName")
        candidates.add(".$className > $titleTagName")
        candidates.add("$containerTag.$className $titleTagName")
        candidates.add("$containerTag.$className a[href] > $titleTagName")
      }
    val id = container.id().trim()
    if (isMeaningfulCssToken(id)) {
      candidates.add("#$id $titleTagName")
      candidates.add("#$id a[href] > $titleTagName")
    }
  }

  private fun addContainerSelectors(
    container: Element?,
    candidates: MutableSet<String>,
  ) {
    if (container == null) {
      return
    }
    val tagName = container.tagName()
    container.classNames()
      .filter { isMeaningfulCssToken(it) }
      .take(3)
      .forEach { className ->
        candidates.add(".$className a[href]")
        candidates.add(".$className > a[href]")
        candidates.add("$tagName.$className a[href]")
      }
    val id = container.id().trim()
    if (isMeaningfulCssToken(id)) {
      candidates.add("#$id a[href]")
    }
  }

  private fun buildCompactPathSelector(element: Element): String? {
    val segments = mutableListOf<String>()
    var cursor: Element? = element
    var depth = 0
    while (cursor != null && depth < 5) {
      val segment = buildSelectorSegment(cursor)
        ?: break
      segments.add(segment)
      if (segment.startsWith("#")) {
        break
      }
      cursor = cursor.parent()
      depth += 1
      if (cursor?.tagName() in setOf("html", "body")) {
        break
      }
    }
    if (segments.size < 2) {
      return null
    }
    return segments
      .asReversed()
      .joinToString(" > ")
  }

  private fun buildSelectorSegment(element: Element): String? {
    val id = element.id().trim()
    if (isMeaningfulCssToken(id)) {
      return "#$id"
    }
    val className = element.classNames()
      .firstOrNull { className -> isMeaningfulCssToken(className) }
    if (className != null) {
      return "${element.tagName()}.$className"
    }
    val tagName = element.tagName()
    if (tagName in setOf("div", "span")) {
      return null
    }
    return tagName
  }

  private fun isLikelySelectorTargetElement(element: Element): Boolean {
    val text = normalizeWhitespace(element.text())
    if (text.length !in 8..220) {
      return false
    }
    if (!text.any { character -> character.isLetterOrDigit() }) {
      return false
    }
    if (element.childrenSize() > 30) {
      return false
    }
    val lower = text.lowercase(Locale.ROOT)
    val blacklist = listOf(
      "home", "about", "contact", "privacy", "terms", "login", "sign in",
      "menu", "subscribe", "rss", "검색", "전체보기", "더보기",
      "카테고리", "공지", "공지사항", "태그", "이전", "다음"
    )
    return blacklist.none { blocked -> lower == blocked || lower.contains(blocked) }
  }

  private fun isMeaningfulCssToken(token: String): Boolean {
    val trimmed = token.trim()
    if (trimmed.length !in 3..40) {
      return false
    }
    if (trimmed.first().isDigit()) {
      return false
    }
    if (!trimmed.all { it.isLetterOrDigit() || it == '-' || it == '_' }) {
      return false
    }
    val digitCount = trimmed.count { it.isDigit() }
    if (digitCount >= trimmed.length / 2) {
      return false
    }
    val lowerCased = trimmed.lowercase(Locale.ROOT)
    if (lowerCased in setOf("active", "selected", "current", "open", "close", "on", "off")) {
      return false
    }
    return true
  }

  private fun countInternalUrls(
    blogUrl: String,
    matches: List<CssSelectorTestMatchResponse>,
  ): Int {
    return matches.count { isInternalUrl(blogUrl, it.url) }
  }

  private fun isInternalUrl(
    blogUrl: String,
    targetUrl: String,
  ): Boolean {
    val baseHost = runCatching { URI(blogUrl).host }.getOrNull()
      ?: return false
    val targetHost = runCatching { URI(targetUrl).host }.getOrNull()
      ?: return false
    return baseHost == targetHost
  }

  private fun calculateConfidence(
    blogUrl: String,
    matchedElementCount: Int,
    matches: List<CssSelectorTestMatchResponse>,
  ): Double {
    if (matches.isEmpty()) {
      return 0.0
    }
    val internalUrlCount = countInternalUrls(blogUrl, matches)
    val internalRatio = internalUrlCount.toDouble() / matches.size.toDouble()
    val titleQualityRatio = matches.count { isLikelyArticleTitle(it.title) }.toDouble() / matches.size.toDouble()
    val coverageScore = matches.size.coerceAtMost(12).toDouble() / 12.0

    var score = internalRatio * 0.45 + titleQualityRatio * 0.35 + coverageScore * 0.2
    if (matchedElementCount > 80) {
      score -= 0.25
    }
    if (matches.size > 50) {
      score -= 0.15
    }
    if (matches.size < 2) {
      score -= 0.25
    }
    return round3(score.coerceIn(0.0, 1.0))
  }

  private fun isLikelyArticleTitle(title: String): Boolean {
    val normalized = title.trim()
    if (normalized.length !in 8..140) {
      return false
    }
    val lower = normalized.lowercase(Locale.ROOT)
    val blacklist = listOf(
      "home", "about", "contact", "privacy", "terms", "login", "sign in",
      "menu", "subscribe", "rss", "검색", "전체보기", "더보기"
    )
    return blacklist.none { lower == it || lower.contains(it) }
  }

  private fun round3(value: Double): Double {
    return kotlin.math.round(value * 1000.0) / 1000.0
  }

  private data class SelectorHintMatch(
    val score: Double,
    val titleMatched: Boolean,
    val urlMatched: Boolean,
  )

  private fun calculateHintMatch(
    matches: List<CssSelectorTestMatchResponse>,
    expectedTitle: String?,
    expectedUrl: String?,
  ): SelectorHintMatch {
    if (matches.isEmpty()) {
      return SelectorHintMatch(score = 0.0, titleMatched = false, urlMatched = false)
    }

    val urlMatched = expectedUrl != null && matches.any { match ->
      val targetUrl = normalizeComparableUrl(match.url)
      targetUrl != null && targetUrl == expectedUrl
    }

    val titleSimilarity = if (expectedTitle == null) {
      0.0
    } else {
      matches.maxOfOrNull { match -> calculateTitleSimilarity(expectedTitle, match.title) } ?: 0.0
    }
    val titleMatched = expectedTitle != null && titleSimilarity >= 0.72

    val score = when {
      expectedTitle != null && expectedUrl != null -> {
        val urlScore = if (urlMatched) 1.0 else 0.0
        (urlScore * 0.7) + (titleSimilarity * 0.3)
      }
      expectedUrl != null -> if (urlMatched) 1.0 else 0.0
      expectedTitle != null -> titleSimilarity
      else -> 0.0
    }
    return SelectorHintMatch(
      score = round3(score.coerceIn(0.0, 1.0)),
      titleMatched = titleMatched,
      urlMatched = urlMatched,
    )
  }

  private fun calculateCombinedConfidence(
    baseConfidence: Double,
    hintScore: Double,
    hasExpectedTitle: Boolean,
    hasExpectedUrl: Boolean,
    titleMatched: Boolean,
    urlMatched: Boolean,
  ): Double {
    if (!hasExpectedTitle && !hasExpectedUrl) {
      return baseConfidence
    }
    var score = (baseConfidence * 0.58) + (hintScore * 0.42)
    if (hasExpectedUrl && !urlMatched) {
      score -= 0.2
    }
    if (hasExpectedTitle && !titleMatched) {
      score -= 0.08
    }
    return round3(score.coerceIn(0.0, 1.0))
  }

  private fun normalizeHintText(value: String): String? {
    val normalized = value
      .lowercase(Locale.ROOT)
      .replace(Regex("\\s+"), " ")
      .trim()
    return normalized.takeIf { it.isNotBlank() }
  }

  private fun normalizeComparableUrl(value: String): String? {
    val trimmed = value.trim()
    if (trimmed.isBlank()) {
      return null
    }
    val uri = runCatching { URI(trimmed).normalize() }.getOrNull()
      ?: return null
    val host = uri.host?.lowercase(Locale.ROOT)
      ?: return null
    val path = uri.path
      ?.trim()
      ?.trimEnd('/')
      ?.ifBlank { "/" }
      ?: "/"
    return "$host$path"
  }

  private fun calculateTitleSimilarity(
    expectedTitle: String,
    candidateTitle: String,
  ): Double {
    val normalizedCandidate = normalizeHintText(candidateTitle) ?: return 0.0
    if (normalizedCandidate == expectedTitle) {
      return 1.0
    }
    if (
      normalizedCandidate.length >= 8 &&
      expectedTitle.length >= 8 &&
      (normalizedCandidate.contains(expectedTitle) || expectedTitle.contains(normalizedCandidate))
    ) {
      return 0.9
    }

    val expectedTokens = tokenizeForSimilarity(expectedTitle)
    val candidateTokens = tokenizeForSimilarity(normalizedCandidate)
    if (expectedTokens.isEmpty() || candidateTokens.isEmpty()) {
      return 0.0
    }
    val intersection = expectedTokens.intersect(candidateTokens).size.toDouble()
    val precision = intersection / expectedTokens.size.toDouble()
    val recall = intersection / candidateTokens.size.toDouble()
    return ((precision * 0.65) + (recall * 0.35)).coerceIn(0.0, 1.0)
  }

  private fun tokenizeForSimilarity(text: String): Set<String> {
    return text
      .lowercase(Locale.ROOT)
      .split(Regex("[^\\p{L}\\p{N}]+"))
      .map { it.trim() }
      .filter { it.length >= 2 }
      .toSet()
  }
}
