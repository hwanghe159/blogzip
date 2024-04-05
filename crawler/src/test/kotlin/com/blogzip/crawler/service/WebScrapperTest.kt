package com.blogzip.crawler.service

import org.junit.jupiter.api.Test

class WebScrapperTest {

    @Test
    fun getArticleUrls() {
        val webScrapper = WebScrapper(HtmlCompressor())
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
}