package com.blogzip.crawler.service

import com.blogzip.crawler.config.SeleniumProperties
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.web.reactive.function.client.WebClient

class WebScrapperTest {

    private lateinit var webScrapper: WebScrapper

    @BeforeEach
    fun setUp() {
        this.webScrapper = WebScrapper(
            WebClient.create(),
            HtmlCompressor(),
            SeleniumProperties(emptyList())
        )
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
"""
    )
    fun getArticles(blogUrl: String, cssSelector: String) {
        val articles = webScrapper.getArticles(blogUrl, cssSelector)

        assertThat(articles).isNotEmpty()
        articles.forEach { article ->
            assertThat(article.title).isNotBlank()
            assertThat(article.url).isNotBlank()
        }
    }

    @Test
    fun convertToRss() {
        val rss = webScrapper.convertToRss("https://velog.io/@jay/posts")
        println(rss)
    }

    @Test
    fun getImageUrl() {
        val result = mutableMapOf<String, String?>()
        for (s in listOf(
            "https://techblog.woowahan.com",
            "https://d2.naver.com/helloworld",
            "https://techblog.lycorp.co.jp/ko",
            "https://hyperconnect.github.io",
            "https://blog.banksalad.com/tech",
            "https://tech.socarcorp.kr",
            "https://techblog.yogiyo.co.kr",
            "https://toss.tech",
            "https://techblog.gccompany.co.kr",
            "https://medium.com/daangn",
            "https://medium.com/watcha",
            "https://meetup.nhncloud.com",
            "https://tech.kakao.com",
            "http://thefarmersfront.github.io",
            "https://medium.com/coupang-engineering/kr/home",
            "https://jojoldu.tistory.com",
            "https://medium.com/zigbang",
            "https://saramin.github.io",
            "https://smilegate.ai",
            "https://techtopic.skplanet.com",
            "https://tech.kakaopay.com",
            "https://medium.com/29cm",
            "https://medium.com/wantedjobs",
            "https://medium.com/cj-onstyle",
            "https://dev.gmarket.com",
            "https://techblog.lotteon.com",
            "https://danawalab.github.io",
            "https://engineering.ab180.co",
            "https://techblog.tabling.co.kr",
            "https://news.hada.io",
            "https://spoqa.github.io",
            "https://netmarble.engineering",
            "https://yozm.wishket.com/magazine/list/develop",
            "https://devocean.sk.com",
        )) {
            val imageUrl = try {
                webScrapper.getImageUrl(s)
            } catch (e: Exception) {
                null
            }
            result[s] = imageUrl
        }
        result.forEach { println(it.key + "," + it.value) }
    }
}