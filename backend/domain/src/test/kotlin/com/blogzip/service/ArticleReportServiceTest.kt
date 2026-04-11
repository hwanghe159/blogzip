package com.blogzip.service

import com.blogzip.common.DomainException
import com.blogzip.common.ErrorCode
import com.blogzip.domain.ArticleReport
import com.blogzip.domain.ArticleReportRepository
import com.blogzip.domain.ArticleRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoSettings

@MockitoSettings
class ArticleReportServiceTest {

  @Mock
  lateinit var articleRepository: ArticleRepository

  @Mock
  lateinit var articleReportRepository: ArticleReportRepository

  @InjectMocks
  lateinit var articleReportService: ArticleReportService

  @DisplayName("같은 사용자가 같은 게시물을 신고하면 기존 신고를 업데이트한다.")
  @Test
  fun report_UpdateExisting() {
    val existing = ArticleReport(
      id = 10,
      articleId = 1,
      userId = 100,
      reason = "기존 사유",
      detail = "기존 상세",
    )
    `when`(articleRepository.existsById(1L))
      .thenReturn(true)
    `when`(articleReportRepository.findByArticleIdAndUserId(1L, 100L))
      .thenReturn(existing)

    val report = articleReportService.report(
      userId = 100,
      articleId = 1,
      reason = "  새로운 사유  ",
      detail = "  새로운 상세  ",
    )

    assertEquals(10L, report.id)
    assertEquals("새로운 사유", report.reason)
    assertEquals("새로운 상세", report.detail)
    verify(articleReportRepository, never()).save(any())
  }

  @DisplayName("게시물이 없으면 신고할 수 없다.")
  @Test
  fun report_FailWhenArticleNotFound() {
    `when`(articleRepository.existsById(1L))
      .thenReturn(false)

    val exception = assertThrows(DomainException::class.java) {
      articleReportService.report(
        userId = 100,
        articleId = 1,
        reason = "사유",
        detail = null,
      )
    }
    assertEquals(ErrorCode.ARTICLE_NOT_FOUND, exception.errorCode)
  }
}
