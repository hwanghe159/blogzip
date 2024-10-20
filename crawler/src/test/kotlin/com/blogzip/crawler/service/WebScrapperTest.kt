package com.blogzip.crawler.service

import com.blogzip.crawler.config.SeleniumProperties
import com.blogzip.crawler.config.WebDriverConfig
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.web.reactive.function.client.WebClient

class WebScrapperTest {

    private lateinit var webScrapper: WebScrapper
    private val webDriver = WebDriverConfig(SeleniumProperties(emptyList())).webDriver()

    @BeforeEach
    fun setUp() {
        this.webScrapper = WebScrapper(
            WebClient.create(),
            HtmlCompressor(),
            webDriver,
        )
    }

    @AfterEach
    fun tearDown() {
        webDriver.quit()
    }

    @DisplayName("blog url과 css selector로 글의 제목과 링크를 추출한다.")
    @ParameterizedTest
    @CsvSource(
        delimiter = '|',
        textBlock = """
        https://d2.naver.com/helloworld | #container > div > div > div > div > h2 > a
        https://engineering.ab180.co | #__next > div > div > div > div > div > div > div > div > div > div > div > div > div > div > div > a > div.css-5gv4hl
        https://danawalab.github.io | body > div.content > div > section > div > div > a > h3
        https://medium.com/coupang-engineering/kr/home | div > div.u-marginBottom40.js-categoryStream > div > section > div > div > div.u-lineHeightBase.postItem > a
        https://devocean.sk.com/tech | #wrapper > div > section > div > div > div > ul > li > div > div > div > a > h3
        https://techblog.woowahan.com | body > div > div > div > div > div > div:not(.firstpaint) > a > h2
        https://blog.gangnamunni.com/blog | #content > div > div > ul > li > div.post-info > div.post-title > a
        https://ridicorp.com/story-category/tech-blog | [id^="post-"] > div > h3 > a
"""
    )
    fun getArticles(blogUrl: String, cssSelector: String) {
        val articles = webScrapper.getArticles(blogUrl, cssSelector).articles
        articles.forEach { a ->
            println("- ${a.title}(${a.url})")
        }

        assertThat(articles).isNotEmpty()
        articles.forEach { article ->
            assertThat(article.title).isNotBlank()
            assertThat(article.url).isNotBlank()
        }
    }
}