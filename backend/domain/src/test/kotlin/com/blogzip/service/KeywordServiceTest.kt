package com.blogzip.service

import com.blogzip.domain.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName

import org.junit.jupiter.api.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoSettings
import org.springframework.data.repository.findByIdOrNull
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
        val keyword1 = Keyword(id = 1, value = "백엔드")
        val keyword2 = Keyword(id = 2, value = "backend").follow(keyword1)
        val keyword3 = Keyword(id = 3, value = "server").follow(keyword1)
        val keyword4 = Keyword(id = 4, value = "프론트엔드")
        val article = mock(Article::class.java)

        `when`(articleRepository.findById(anyLong()))
            .thenReturn(Optional.of(article))
        `when`(keywordRepository.findAllByValueIn(anyCollection()))
            .thenReturn(listOf(keyword1, keyword2, keyword3, keyword4))
        `when`(articleKeywordRepository.findAllByArticleId(anyLong()))
            .thenReturn(emptyList())

        keywordService.addArticleKeywords(1, listOf("백엔드", "server", "redis"))

        verify(keywordRepository, times(1)).saveAll(anyCollection())
    }
}