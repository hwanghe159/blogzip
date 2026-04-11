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

@Service
class ArticleQueryService(
  private val articleRepository: ArticleRepository,
  private val articleSummaryRepository: ArticleSummaryRepository,
  private val keywordRepository: KeywordRepository,
  private val blogRepository: BlogRepository,
  private val readLaterRepository: ReadLaterRepository,
  private val userRepository: UserRepository,
) {

  var log = logger()

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

  // 키워드에 해당하는 글 조회 (비로그인)
  @Transactional(readOnly = true)
  fun searchByKeywordId(keywordId: Long, next: Long?, size: Int): SearchedArticles {
    val headKeywordId = getHeadKeywordId(keywordId)
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
    val articles = articleRepository.searchByHeadKeyword(
      headKeywordId = headKeywordId,
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
    val articles = articleRepository.searchByHeadKeyword(
      headKeywordId = headKeywordId,
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
