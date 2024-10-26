//package com.blogzip.batch.summarize
//
//import com.blogzip.batch.summarize.ArticleContentSummarizer.SummarizeResult
//import com.blogzip.domain.Article
//import com.blogzip.domain.ArticleRepository
//import com.blogzip.slack.SlackSender
//import com.blogzip.service.ArticleService
//import io.mockk.coEvery
//import io.mockk.every
//import io.mockk.mockk
//import org.junit.jupiter.api.Assertions.*
//import org.junit.jupiter.api.Disabled
//
//import org.junit.jupiter.api.Test
//import org.springframework.beans.factory.annotation.Autowired
//import org.springframework.boot.test.context.SpringBootTest
//import org.springframework.boot.test.mock.mockito.MockBean
//import org.springframework.retry.annotation.EnableRetry
//import org.springframework.test.context.ActiveProfiles
//import java.time.LocalDate
//
//@SpringBootTest
//@ActiveProfiles("test")
//class ArticleContentSummarizeServiceTest {
//
//    private lateinit var articleContentSummarizeService: ArticleContentSummarizeService
//
//    @MockBean
//    private lateinit var articleService: ArticleService
//
//    @MockBean
//    private lateinit var articleContentSummarizer: ArticleContentSummarizer
//
//    @MockBean
//    private lateinit var slackSender: SlackSender
//
//    // todo 테스트 작성
//    @Disabled
//    @Test
//    fun `요약 실패시 재시도한다`() {
//        val startDate = LocalDate.of(2024, 1, 1)
//        val article1: Article = mockk()
//        val article2: Article = mockk()
//        val article3: Article = mockk()
//        val summarizeResult = SummarizeResult("요약된 내용", "model-id")
//
//        every { article1.id } returns 1
//        every { article1.content } returns "한번에 성공"
//        every { article2.id } returns 2
//        every { article2.content } returns "한번 실패 후 성공"
//        every { article3.id } returns 3
//        every { article3.content } returns "두번 실패 후 성공"
//
//        every { articleService.findAllSummarizeTarget(any<LocalDate>()) } returns
//                listOf(article1, article2, article3)
//        coEvery { articleContentSummarizer.summarize("한번에 성공") }
//            .returns(summarizeResult)
//        coEvery { articleContentSummarizer.summarize("한번 실패 후 성공") }
//            .returns(null)
//            .andThen(summarizeResult)
//        coEvery { articleContentSummarizer.summarize("두번 실패 후 성공") }
//            .returns(null)
//            .andThen(null)
//            .andThen(summarizeResult)
//        every { articleService.updateSummary(any(), any(), any()) } returns Unit
//        every { slackSender.sendMessageAsync(any(), any()) } returns Unit
//
//        articleContentSummarizeService.summarize(startDate)
//    }
//}