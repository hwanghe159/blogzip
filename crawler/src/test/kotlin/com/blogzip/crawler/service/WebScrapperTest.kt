package com.blogzip.crawler.service

import com.blogzip.crawler.config.SeleniumProperties
import org.junit.jupiter.api.Test
import org.springframework.web.reactive.function.client.WebClient

class WebScrapperTest {

    private val webScrapper = WebScrapper(
        WebClient.create(), HtmlCompressor(), SeleniumProperties(
            emptyList()
        )
    )

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