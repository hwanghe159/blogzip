package com.blogzip.service

import com.blogzip.domain.Article
import com.blogzip.domain.ArticleRepository
import com.blogzip.domain.ArticleSummary
import com.blogzip.domain.ArticleSummaryRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoSettings
import java.time.LocalDate
import java.util.Optional

@MockitoSettings
class ArticleCommandServiceTest {

  @Mock
  lateinit var articleRepository: ArticleRepository

  @Mock
  lateinit var articleSummaryRepository: ArticleSummaryRepository

  @InjectMocks
  lateinit var articleCommandService: ArticleCommandService

  @DisplayName("게시글 요약을 갱신하면 요약 이력 row를 추가하고 적용 상태로 만든다.")
  @Test
  fun updateSummary() {
    val article = Article(
      id = 1,
      blogId = 100,
      title = "제목",
      content = "내용",
      url = "https://blogzip.test/article/1",
      createdDate = LocalDate.of(2026, 4, 1),
    )
    `when`(articleRepository.findById(1L))
      .thenReturn(Optional.of(article))
    `when`(articleSummaryRepository.save(any()))
      .thenAnswer { it.arguments[0] as ArticleSummary }

    articleCommandService.updateSummary(
      id = 1L,
      summary = "새로운 요약",
      summarizedBy = "new-model",
    )

    verify(articleSummaryRepository, times(1)).clearAppliedByArticleId(1L)
    verify(articleSummaryRepository, times(1))
      .save(
        argThat { it.articleId == 1L && it.summary == "새로운 요약" && it.summarizedBy == "new-model" && it.isApplied }
      )
  }

  @DisplayName("요약 이력 row를 적용하면 해당 요약이 article에 반영된다.")
  @Test
  fun applySummary() {
    val article = Article(
      id = 1,
      blogId = 100,
      title = "제목",
      content = "내용",
      url = "https://blogzip.test/article/1",
      createdDate = LocalDate.of(2026, 4, 1),
    )
    val articleSummary = ArticleSummary(
      id = 11,
      articleId = 1,
      summary = "적용할 요약",
      summarizedBy = "candidate-model",
      isApplied = false,
    )
    `when`(articleSummaryRepository.findById(11L))
      .thenReturn(Optional.of(articleSummary))
    `when`(articleRepository.findById(1L))
      .thenReturn(Optional.of(article))

    val applied = articleCommandService.applySummary(11L)

    verify(articleSummaryRepository, times(1)).clearAppliedByArticleId(1L)
    assertEquals(true, applied.isApplied)
  }

  @DisplayName("게시글 생성 날짜를 단건으로 수정할 수 있다.")
  @Test
  fun updateCreatedDate() {
    val article = Article(
      id = 1,
      blogId = 100,
      title = "제목",
      content = "내용",
      url = "https://blogzip.test/article/1",
      createdDate = LocalDate.of(2026, 4, 1),
    )
    `when`(articleRepository.findById(1L))
      .thenReturn(Optional.of(article))

    val result = articleCommandService.updateCreatedDate(
      articleId = 1L,
      createdDate = LocalDate.of(2026, 4, 2),
    )

    assertEquals(LocalDate.of(2026, 4, 1), result.beforeCreatedDate)
    assertEquals(LocalDate.of(2026, 4, 2), result.createdDate)
    assertEquals(LocalDate.of(2026, 4, 2), article.createdDate)
  }

  @DisplayName("게시글 생성 날짜를 벌크로 수정할 수 있다.")
  @Test
  fun updateCreatedDates() {
    val article1 = Article(
      id = 1,
      blogId = 100,
      title = "제목1",
      content = "내용1",
      url = "https://blogzip.test/article/1",
      createdDate = LocalDate.of(2026, 4, 1),
    )
    val article2 = Article(
      id = 2,
      blogId = 100,
      title = "제목2",
      content = "내용2",
      url = "https://blogzip.test/article/2",
      createdDate = LocalDate.of(2026, 4, 1),
    )
    `when`(articleRepository.findAllById(listOf(1L, 2L)))
      .thenReturn(listOf(article1, article2))

    val results = articleCommandService.updateCreatedDates(
      listOf(
        ArticleCreatedDateUpdateCommand(
          articleId = 1L,
          createdDate = LocalDate.of(2026, 4, 2),
        ),
        ArticleCreatedDateUpdateCommand(
          articleId = 2L,
          createdDate = LocalDate.of(2026, 4, 3),
        )
      )
    )

    assertEquals(2, results.size)
    assertEquals(LocalDate.of(2026, 4, 2), article1.createdDate)
    assertEquals(LocalDate.of(2026, 4, 3), article2.createdDate)
  }
}
