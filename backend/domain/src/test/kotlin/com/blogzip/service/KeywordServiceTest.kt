package com.blogzip.service

import com.blogzip.domain.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName

import org.junit.jupiter.api.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoSettings
import java.util.*

@MockitoSettings
class KeywordServiceTest {

  @Mock
  lateinit var keywordRepository: KeywordRepository

  @Mock
  lateinit var articleKeywordRepository: ArticleKeywordRepository

  @Mock
  lateinit var articleRepository: ArticleRepository

  @InjectMocks
  lateinit var keywordService: KeywordService

  @DisplayName("게시글에 키워드 추가 요청이 들어온 경우, 없던 키워드면 대표 키워드로 새로 생성하고, 있는 키워드이고 대표이면 게시물과 연결한다.")
  @Test
  fun addArticleKeywords() {
    val existingKeyword1 = Keyword(id = 1, value = "백엔드")
    val existingKeyword2 = Keyword(id = 2, value = "backend").follow(existingKeyword1)
    val existingKeyword3 = Keyword(id = 3, value = "server").follow(existingKeyword1)
    val existingKeyword4 = Keyword(id = 4, value = "프론트엔드")
    val newKeyword = Keyword(id = 5, value = "redis")
    val article = mock(Article::class.java)

    `when`(articleRepository.findById(anyLong()))
      .thenReturn(Optional.of(article))
    `when`(article.id)
      .thenReturn(1)
    `when`(keywordRepository.findAllByValueIn(anyCollection()))
      .thenReturn(listOf(existingKeyword1, existingKeyword2, existingKeyword3, existingKeyword4))
    `when`(keywordRepository.save(any()))
      .thenReturn(newKeyword)
    `when`(articleKeywordRepository.findAllByArticleId(anyLong()))
      .thenReturn(emptyList())

    keywordService.addArticleKeywords(1, listOf("백엔드", "server", "redis"))

    verify(keywordRepository, times(1)).save(any())
    verify(articleKeywordRepository, times(1))
      .saveAll(argThat<Iterable<ArticleKeyword>> { it.count() == 2 })
  }

  @DisplayName("키워드 개요 조회 시 head/follower/노출 상태와 아티클 매핑 수를 집계한다.")
  @Test
  fun getOverview() {
    val headKeyword1 = Keyword(id = 1, value = "backend", isVisible = true)
    val followerKeyword = Keyword(id = 2, value = "server", head = headKeyword1, isVisible = false)
    val headKeyword2 = Keyword(id = 3, value = "frontend", isVisible = false)

    `when`(keywordRepository.findAllWithHead())
      .thenReturn(listOf(headKeyword1, followerKeyword, headKeyword2))
    `when`(articleKeywordRepository.countMappingsByHeadKeywordId())
      .thenReturn(
        listOf(
          object : ArticleKeywordRepository.HeadKeywordMappingCount {
            override val headKeywordId: Long = 1L
            override val mappingCount: Long = 3L
          }
        )
      )

    val overview = keywordService.getOverview()

    assertEquals(3, overview.totalKeywordCount)
    assertEquals(2, overview.headKeywordCount)
    assertEquals(1, overview.followerKeywordCount)
    assertEquals(1, overview.visibleKeywordCount)
    assertEquals(2, overview.hiddenKeywordCount)

    val backendKeyword = overview.headKeywords.first { it.id == 1L }
    assertEquals(3, backendKeyword.articleCount)
    assertEquals(1, backendKeyword.followers.size)
    assertEquals(false, backendKeyword.followers.first().isVisible)
  }

  @DisplayName("같은 키워드끼리 머지하려는 경우, 아무 동작도 하지 않는다.")
  @Test
  fun mergeById_WithSameKeyword() {
    val keyword = Keyword(id = 1, value = "backend")
    `when`(keywordRepository.findById(1L))
      .thenReturn(Optional.of(keyword))

    keywordService.mergeById(1L, 1L)

    verify(articleKeywordRepository, never()).findAllByHeadKeywordId(anyLong())
  }
}
