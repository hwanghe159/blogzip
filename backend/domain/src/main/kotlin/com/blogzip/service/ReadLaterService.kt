package com.blogzip.service

import com.blogzip.common.DomainException
import com.blogzip.common.ErrorCode
import com.blogzip.domain.*
import com.blogzip.dto.ReadLaterAndArticle
import com.blogzip.dto.SearchedReadLaters
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ReadLaterService(
  private val readLaterRepository: ReadLaterRepository,
  private val userRepository: UserRepository,
  private val articleRepository: ArticleRepository,
  private val blogRepository: BlogRepository,
) {

  @Transactional(readOnly = true)
  fun findAllByUserId(userId: Long): List<ReadLaterAndArticle> {
    val readLaters =
      readLaterRepository.findAllByUserId(userId, Sort.by(Sort.Direction.DESC, "id"))
    val articleIds = readLaters.map { it.articleId }.toSet()
    val articles = articleRepository.findAllById(articleIds).map { it.id to it }.toMap()
    return readLaters
      .filter { articles[it.articleId] != null }
      .map { ReadLaterAndArticle(it, articles[it.articleId]!!) }
  }

  @Transactional(readOnly = true)
  fun search(userId: Long, next: Long?, size: Int): SearchedReadLaters {
    val readLaters = readLaterRepository.search(
      userId,
      next,
      PageRequest.of(
        0, size + 1,
        Sort.by("id").descending()
      )
    )

    val articleIds = readLaters.map { it.articleId }.toSet()
    val articles = articleRepository.findAllById(articleIds).map { it.id to it }.toMap()
    val blogIds = articles.values.map { it.blogId }.toSet()
    val blogs = blogRepository.findAllById(blogIds).map { it.id to it }.toMap()

    val existsNext = readLaters.size == size + 1
    val finalReadLaters = readLaters.take(size)

    return SearchedReadLaters(
      readLaters = finalReadLaters
        .map {
          val article = articles[it.articleId]!!
          val blog = blogs[article.blogId]!!

          com.blogzip.dto.ReadLater(
            id = it.id!!,
            article = com.blogzip.dto.Article(
              id = article.id!!,
              blog = com.blogzip.dto.Blog.from(blog),
              title = article.title,
              content = article.content,
              url = article.url,
              summary = article.summary!!,
              summarizedBy = article.summarizedBy!!,
              createdDate = article.createdDate!!,
            )
          )
        },
      next = if (existsNext) finalReadLaters.last().id else null,
    )
  }

  @Transactional
  fun save(userId: Long, articleId: Long): ReadLaterAndArticle {
    userRepository.findByIdOrNull(userId)
      ?: throw DomainException(ErrorCode.USER_NOT_FOUND)
    val article = (articleRepository.findByIdOrNull(articleId)
      ?: throw DomainException(ErrorCode.ARTICLE_NOT_FOUND))

    val readLater = (readLaterRepository.findByUserIdAndArticleId(userId, articleId)
      ?: readLaterRepository.save(ReadLater(userId = userId, articleId = articleId)))
    return ReadLaterAndArticle(readLater, article)
  }

  @Transactional
  fun delete(userId: Long, articleId: Long) {
    readLaterRepository.deleteAllByUserIdAndArticleId(userId, articleId)
  }
}