package com.blogzip.crawler.service

import com.blogzip.crawler.confg.SeleniumProperties
import org.junit.jupiter.api.Test
import org.springframework.web.reactive.function.client.WebClient

class WebScrapperTest {

    private val webScrapper = WebScrapper(WebClient.create(), HtmlCompressor(), SeleniumProperties(
        emptyList()
    ))

    @Test
    fun getArticleUrls() {
        val links = webScrapper.getArticles(
            "https://medium.com/coupang-engineering/kr/home",
            "div > div.u-marginBottom40.js-categoryStream > div > section > div > div > div.u-lineHeightBase.postItem > a"
        )
        println("링크!!")
        links.forEach { println(it) }

        // div > div.u-marginBottom40.js-categoryStream > div > section > div > div > div.col.u-xs-marginBottom10.u-paddingLeft0.u-paddingRight0.u-paddingTop15.u-marginBottom30 > a
        // div > div.u-marginBottom40.js-categoryStream > div > section > div > div > div.col.u-xs-marginBottom10.u-paddingLeft0.u-paddingRight0.u-paddingTop15.u-marginBottom30 > a
        // div > div.u-marginBottom40.js-categoryStream > div > section > div > div > div.u-lineHeightBase.postItem > a
    }

    @Test
    fun convertToRss() {
        val rss = webScrapper.convertToRss("https://velog.io/@jay/posts")
        println(rss)
    }
}