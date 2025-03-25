package com.blogzip.notification.email

import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.test.context.ContextConfiguration
import org.thymeleaf.spring6.SpringTemplateEngine
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver
import java.time.LocalDate

@SpringBootTest
@ContextConfiguration(classes = [EmailTemplateParserTest.TemplateParserTestConfig::class])
class EmailTemplateParserTest {

  @Autowired
  private lateinit var emailTemplateParser: EmailTemplateParser

  @Test
  fun `타임리프 동작 학습테스트`() {
    val user = User(
      id = 1,
      email = "",
      receiveDates = listOf(LocalDate.EPOCH),
    )
    val articles = listOf(
      Article(
        title = "title1",
        url = "url1",
        summary = "summary1",
        blogName = "blogName1",
        keywords = listOf("키워드1", "키워드2"),
        createdDate = LocalDate.EPOCH,
      ), Article(
        title = "title2",
        url = "url2",
        summary = "summary2",
        blogName = "blogName2",
        keywords = listOf("키워드3", "키워드4"),
        createdDate = LocalDate.EPOCH,
      ), Article(
        title = "title3",
        url = "url3",
        summary = "summary3",
        blogName = "blogName3",
        keywords = emptyList(),
        createdDate = LocalDate.EPOCH,
      )
    )
    assertDoesNotThrow {
      println(
        emailTemplateParser.parseArticles(
          user = user,
          articles = articles
        )
      )
    }
  }

  @Configuration
  class TemplateParserTestConfig {
    @Bean
    fun templateResolver(): ClassLoaderTemplateResolver {
      val templateResolver = ClassLoaderTemplateResolver()
      templateResolver.prefix = "/templates/"
      templateResolver.suffix = ".html"
      templateResolver.setTemplateMode("HTML")
      templateResolver.characterEncoding = "UTF-8"
      return templateResolver
    }

    @Bean
    fun templateEngine(templateResolver: ClassLoaderTemplateResolver): SpringTemplateEngine {
      val templateEngine = SpringTemplateEngine()
      templateEngine.setTemplateResolver(templateResolver)
      return templateEngine
    }

    @Bean
    fun templateParser(templateEngine: SpringTemplateEngine): EmailTemplateParser {
      return EmailTemplateParser(templateEngine)
    }
  }
}