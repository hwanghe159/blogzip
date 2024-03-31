package com.blogzip.batch.email

import com.blogzip.domain.Article
import com.blogzip.domain.Blog
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.test.context.ContextConfiguration
import org.thymeleaf.spring6.SpringTemplateEngine
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver

@SpringBootTest
@ContextConfiguration(classes = [EmailTemplateParserTest.TemplateParserTestConfig::class])
class EmailTemplateParserTest {

    @Autowired
    private lateinit var emailTemplateParser: EmailTemplateParser

    @Mock
    private val blog = mockk<Blog>()

    @Test
    fun `타임리프 동작 학습테스트`() {
        val articles = listOf(
            Article(
                blog = blog,
                title = "title1",
                url = "url1",
                summary = "summary1",
            ), Article(
                blog = blog,
                title = "title2",
                url = "url2",
                summary = "summary2",
            )
        )
        assertDoesNotThrow { emailTemplateParser.parseArticles(articles) }
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