package com.blogzip.service

import com.blogzip.common.DomainException
import com.blogzip.common.ErrorCode
import com.blogzip.domain.*
import com.blogzip.dto.ArticleAndBlog
import com.blogzip.dto.SearchedArticles
import com.blogzip.logger
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

data class AdminRecentArticle(
  val article: Article,
  val blog: Blog,
  val appliedSummary: ArticleSummary?,
)

data class AdminRecentArticles(
  val items: List<AdminRecentArticle>,
  val next: Long?,
)

data class FeedKeywordCount(
  val keyword: String,
  val count: Int,
)

@Service
class ArticleQueryService(
  private val articleRepository: ArticleRepository,
  private val articleSummaryRepository: ArticleSummaryRepository,
  private val articleKeywordRepository: ArticleKeywordRepository,
  private val keywordRepository: KeywordRepository,
  private val blogRepository: BlogRepository,
  private val readLaterRepository: ReadLaterRepository,
  private val userRepository: UserRepository,
) {

  var log = logger()

  private fun emptySearchedArticles(readLaterArticleIds: Set<Long> = emptySet()): SearchedArticles {
    return SearchedArticles.of(
      articleAndBlogs = emptyList(),
      next = null,
      readLaterArticleIds = readLaterArticleIds,
      appliedSummaries = emptyMap(),
    )
  }

  // 메인에 노출될 글 조회 (비로그인)
  @Transactional(readOnly = true)
  fun search(from: LocalDate, to: LocalDate?, next: Long?, size: Int): SearchedArticles {
    val blogs = blogRepository.findAllByIsShowOnMain(true)
      .map { it.id!! to it }.toMap()
    if (blogs.isEmpty()) {
      return SearchedArticles.of(
        articleAndBlogs = emptyList(),
        next = null,
        readLaterArticleIds = emptySet(),
        appliedSummaries = emptyMap(),
      )
    }
    val articles = articleRepository.search(
      blogs.keys,
      from,
      to ?: LocalDate.now(),
      next,
      articlePageRequest(size)
    )
    val existsNext = articles.size == size + 1
    val finalArticles = articles.take(size)
    val appliedSummaries = articleSummaryRepository
      .findAllByArticleIdInAndIsAppliedTrue(finalArticles.mapNotNull { it.id })
      .associateBy { it.articleId }

    return SearchedArticles.of(
      articleAndBlogs = finalArticles
        .filter { blogs[it.blogId] != null }
        .map { ArticleAndBlog(it, blogs[it.blogId]!!) },
      next = if (existsNext) finalArticles.last().id else null,
      readLaterArticleIds = emptySet(),
      appliedSummaries = appliedSummaries,
    )
  }

  // 메인에 노출될 글 조회 (로그인)
  @Transactional(readOnly = true)
  fun searchMy(
    from: LocalDate,
    to: LocalDate?,
    next: Long?,
    size: Int,
    userId: Long,
  ): SearchedArticles {
    // fetch join과 페이지네이션을 같이 사용하면 데이터를 전부 가져와 메모리에서 거른다.
    // 이를 방지하기 위해 2개의 쿼리로 나눔.
    val user = (userRepository.findByIdOrNull(userId)
      ?: throw DomainException(ErrorCode.USER_NOT_FOUND))
    val blogIds = user.getAllSubscribingBlogIds()
    if (blogIds.isEmpty()) {
      return SearchedArticles.of(
        articleAndBlogs = emptyList(),
        next = null,
        readLaterArticleIds = emptySet(),
        appliedSummaries = emptyMap(),
      )
    }
    val articles = articleRepository.search(
      blogIds,
      from,
      to ?: LocalDate.now(),
      next,
      articlePageRequest(size)
    )
    val blogs = blogRepository.findAllById(blogIds).map { it.id to it }.toMap()
    val readLaterArticleIds = readLaterRepository.findAllByUserId(userId, null)
      .map { it.articleId }
      .toSet()

    val existsNext = articles.size == size + 1
    val finalArticles = articles.take(size)
    val appliedSummaries = articleSummaryRepository
      .findAllByArticleIdInAndIsAppliedTrue(finalArticles.mapNotNull { it.id })
      .associateBy { it.articleId }

    return SearchedArticles.of(
      articleAndBlogs = finalArticles
        .filter { blogs[it.blogId] != null }
        .map { ArticleAndBlog(it, blogs[it.blogId]!!) },
      next = if (existsNext) finalArticles.last().id else null,
      readLaterArticleIds,
      appliedSummaries = appliedSummaries,
    )
  }

  @Transactional(readOnly = true)
  fun searchByKeywordValue(
    keywordValue: String,
    next: Long?,
    size: Int,
  ): SearchedArticles {
    return searchByKeywordValues(listOf(keywordValue), next, size)
  }

  @Transactional(readOnly = true)
  fun searchByKeywordValues(
    keywordValues: Collection<String>,
    next: Long?,
    size: Int,
  ): SearchedArticles {
    val headKeywordIds = findHeadKeywordIds(keywordValues)
    if (headKeywordIds.isEmpty()) {
      return emptySearchedArticles()
    }
    return searchByHeadKeywordIds(headKeywordIds, next, size)
  }

  @Transactional(readOnly = true)
  fun searchMyByKeywordValue(
    keywordValue: String,
    next: Long?,
    size: Int,
    userId: Long,
  ): SearchedArticles {
    return searchMyByKeywordValues(listOf(keywordValue), next, size, userId)
  }

  @Transactional(readOnly = true)
  fun searchMyByKeywordValues(
    keywordValues: Collection<String>,
    next: Long?,
    size: Int,
    userId: Long,
  ): SearchedArticles {
    val headKeywordIds = findHeadKeywordIds(keywordValues)
    if (headKeywordIds.isEmpty()) {
      return emptySearchedArticles()
    }
    return searchMyByHeadKeywordIds(
      headKeywordIds = headKeywordIds,
      next = next,
      size = size,
      userId = userId,
    )
  }

  // 키워드에 해당하는 글 조회 (비로그인)
  @Transactional(readOnly = true)
  fun searchByKeywordId(keywordId: Long, next: Long?, size: Int): SearchedArticles {
    val headKeywordId = getHeadKeywordId(keywordId)
    return searchByHeadKeywordIds(listOf(headKeywordId), next, size)
  }

  private fun searchByHeadKeywordIds(
    headKeywordIds: Collection<Long>,
    next: Long?,
    size: Int,
  ): SearchedArticles {
    val blogs = blogRepository.findAllByIsShowOnMain(true)
      .map { it.id!! to it }.toMap()
    if (blogs.isEmpty()) {
      return SearchedArticles.of(
        articleAndBlogs = emptyList(),
        next = null,
        readLaterArticleIds = emptySet(),
        appliedSummaries = emptyMap(),
      )
    }
    val articles = articleRepository.searchByHeadKeywords(
      headKeywordIds = headKeywordIds,
      blogIds = blogs.keys,
      next = next,
      pageable = articlePageRequest(size),
    )
    val existsNext = articles.size == size + 1
    val finalArticles = articles.take(size)
    val appliedSummaries = articleSummaryRepository
      .findAllByArticleIdInAndIsAppliedTrue(finalArticles.mapNotNull { it.id })
      .associateBy { it.articleId }

    return SearchedArticles.of(
      articleAndBlogs = finalArticles
        .filter { blogs[it.blogId] != null }
        .map { ArticleAndBlog(it, blogs[it.blogId]!!) },
      next = if (existsNext) finalArticles.last().id else null,
      readLaterArticleIds = emptySet(),
      appliedSummaries = appliedSummaries,
    )
  }

  // 키워드에 해당하는 글 조회 (로그인)
  @Transactional(readOnly = true)
  fun searchMyByKeywordId(
    keywordId: Long,
    next: Long?,
    size: Int,
    userId: Long,
  ): SearchedArticles {
    val headKeywordId = getHeadKeywordId(keywordId)
    return searchMyByHeadKeywordIds(
      headKeywordIds = listOf(headKeywordId),
      next = next,
      size = size,
      userId = userId,
    )
  }

  @Transactional(readOnly = true)
  fun countVisibleKeywordsForFeed(
    from: LocalDate,
    to: LocalDate,
  ): List<FeedKeywordCount> {
    val blogs = blogRepository.findAllByIsShowOnMain(true)
      .mapNotNull { blog -> blog.id }
    if (blogs.isEmpty()) {
      return emptyList()
    }
    return articleKeywordRepository.countVisibleHeadKeywordsForFeed(
      blogIds = blogs,
      from = from,
      to = to,
    )
      .map { count ->
        FeedKeywordCount(
          keyword = count.keywordValue,
          count = count.mappingCount.toInt(),
        )
      }
      .sortedWith(
        compareByDescending<FeedKeywordCount> { it.count }
          .thenBy { it.keyword.lowercase() }
      )
  }

  @Transactional(readOnly = true)
  fun countVisibleKeywordsForMyFeed(
    from: LocalDate,
    to: LocalDate,
    userId: Long,
  ): List<FeedKeywordCount> {
    val user = (userRepository.findByIdOrNull(userId)
      ?: throw DomainException(ErrorCode.USER_NOT_FOUND))
    val subscribedBlogIds = user.getAllSubscribingBlogIds()
    if (subscribedBlogIds.isEmpty()) {
      return emptyList()
    }
    return articleKeywordRepository.countVisibleHeadKeywordsForFeed(
      blogIds = subscribedBlogIds,
      from = from,
      to = to,
    )
      .map { count ->
        FeedKeywordCount(
          keyword = count.keywordValue,
          count = count.mappingCount.toInt(),
        )
      }
      .sortedWith(
        compareByDescending<FeedKeywordCount> { it.count }
          .thenBy { it.keyword.lowercase() }
      )
  }

  private fun searchMyByHeadKeywordIds(
    headKeywordIds: Collection<Long>,
    next: Long?,
    size: Int,
    userId: Long,
  ): SearchedArticles {
    val user = (userRepository.findByIdOrNull(userId)
      ?: throw DomainException(ErrorCode.USER_NOT_FOUND))
    val blogIds = user.getAllSubscribingBlogIds()
    if (blogIds.isEmpty()) {
      return SearchedArticles.of(
        articleAndBlogs = emptyList(),
        next = null,
        readLaterArticleIds = emptySet(),
        appliedSummaries = emptyMap(),
      )
    }
    val articles = articleRepository.searchByHeadKeywords(
      headKeywordIds = headKeywordIds,
      blogIds = blogIds,
      next = next,
      pageable = articlePageRequest(size),
    )
    val blogs = blogRepository.findAllById(blogIds).map { it.id to it }.toMap()
    val readLaterArticleIds = readLaterRepository.findAllByUserId(userId, null)
      .map { it.articleId }
      .toSet()

    val existsNext = articles.size == size + 1
    val finalArticles = articles.take(size)
    val appliedSummaries = articleSummaryRepository
      .findAllByArticleIdInAndIsAppliedTrue(finalArticles.mapNotNull { it.id })
      .associateBy { it.articleId }

    return SearchedArticles.of(
      articleAndBlogs = finalArticles
        .filter { blogs[it.blogId] != null }
        .map { ArticleAndBlog(it, blogs[it.blogId]!!) },
      next = if (existsNext) finalArticles.last().id else null,
      readLaterArticleIds,
      appliedSummaries = appliedSummaries,
    )
  }

  private fun findHeadKeywordIds(keywordValues: Collection<String>): List<Long> {
    val normalizedKeywords = keywordValues
      .map { it.trim() }
      .filter { it.isNotBlank() }
      .distinct()
    if (normalizedKeywords.isEmpty()) {
      return emptyList()
    }
    return keywordRepository.findAllByValueIn(normalizedKeywords)
      .mapNotNull { keyword -> keyword.head?.id ?: keyword.id }
      .distinct()
  }

  @Transactional(readOnly = true)
  fun existsByUrl(url: String): Boolean {
    return articleRepository.existsByUrl(url)
  }

  @Transactional(readOnly = true)
  fun findById(id: Long): Article {
    return articleRepository.findByIdOrNull(id)
      ?: throw DomainException(ErrorCode.ARTICLE_NOT_FOUND)
  }

  @Transactional(readOnly = true)
  fun findAllSummarizeTarget(startDate: LocalDate): List<Article> {
    return articleRepository.findAllByCreatedDateGreaterThanEqualAndNoAppliedSummary(startDate)
  }

  @Transactional(readOnly = true)
  fun findAllByBlogIdsAndCreatedDates(
    blogIds: Collection<Long>,
    createdDates: Collection<LocalDate>,
  ): List<Article> {
    return articleRepository.findAllByBlogIdInAndCreatedDateIn(blogIds, createdDates)
  }

  @Transactional(readOnly = true)
  fun findAllByBlogId(blogId: Long): List<Article> {
    return articleRepository.findAllByBlogId(blogId)
  }

  @Transactional(readOnly = true)
  fun findAllById(articleIds: List<Long>): List<Article> {
    return articleRepository.findAllById(articleIds)
  }

  @Transactional(readOnly = true)
  fun searchRecentForAdmin(next: Long?, size: Int): AdminRecentArticles {
    val articles = articleRepository.searchRecent(next, articlePageRequest(size))
    if (articles.isEmpty()) {
      return AdminRecentArticles(items = emptyList(), next = null)
    }

    val existsNext = articles.size == size + 1
    val finalArticles = articles.take(size)
    val blogsById = blogRepository.findAllById(finalArticles.map { it.blogId }.distinct())
      .associateBy { it.id!! }
    val appliedSummaries = articleSummaryRepository
      .findAllByArticleIdInAndIsAppliedTrue(finalArticles.mapNotNull { it.id })
      .associateBy { it.articleId }

    val items = finalArticles.mapNotNull { article ->
      val blog = blogsById[article.blogId] ?: return@mapNotNull null
      AdminRecentArticle(
        article = article,
        blog = blog,
        appliedSummary = article.id?.let { appliedSummaries[it] },
      )
    }
    return AdminRecentArticles(
      items = items,
      next = if (existsNext) finalArticles.last().id else null,
    )
  }

  private fun getHeadKeywordId(keywordId: Long): Long {
    val keyword = keywordRepository.findByIdOrNull(keywordId)
      ?: throw DomainException(ErrorCode.KEYWORD_NOT_FOUND)
    return keyword.head?.id ?: keyword.id!!
  }

  private fun articlePageRequest(size: Int): PageRequest {
    return PageRequest.of(
      0, size + 1,
      Sort.by("createdDate").descending()
        .and(Sort.by("id").descending())
    )
  }
}
