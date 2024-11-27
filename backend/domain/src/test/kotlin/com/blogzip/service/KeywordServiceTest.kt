package com.blogzip.service

import com.blogzip.domain.*
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
}