package com.blogzip.crawler.service

import com.blogzip.crawler.confg.WebClientConfig
import org.junit.jupiter.api.BeforeEach

import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class RomeTest {

    private lateinit var rssFeedFetcher: RssFeedFetcher
    private lateinit var xmlParser: XmlParser

    @BeforeEach
    fun setUp() {
        val webClientConfig = WebClientConfig()
        this.xmlParser = XmlParser(HtmlCompressor())
        this.rssFeedFetcher = RssFeedFetcher(webClientConfig.xmlWebClient(), this.xmlParser)
    }

    @ParameterizedTest
    @CsvSource(
        value = [
            "http://thefarmersfront.github.io/feed.xml",
            "https://blog.banksalad.com/rss.xml",
            "https://ebay-korea.tistory.com/rss",
            "https://hyperconnect.github.io/feed.xml",
            "https://jojoldu.tistory.com/rss",
            "https://medium.com/feed/29cm",
            "https://medium.com/feed/cj-onstyle",
            "https://medium.com/feed/daangn",
            "https://medium.com/feed/wantedjobs",
            "https://medium.com/feed/watcha",
            "https://medium.com/feed/zigbang",
            "https://meetup.nhncloud.com/rss",
            "https://netmarble.engineering/feed/",
            "https://news.hada.io/rss/news", // 실패
            "https://saramin.github.io/feed.xml",
            "https://smilegate.ai/feed/",
            "https://spoqa.github.io/atom.xml",
            "https://tech.kakao.com/feed/",
            "https://tech.kakaopay.com/rss",
            "https://tech.socarcorp.kr/feed.xml",
            "https://techblog.gccompany.co.kr/feed",
            "https://techblog.lotteon.com/feed",
            "https://techblog.lycorp.co.jp/ko/feed/index.xml",
            "https://techblog.tabling.co.kr/feed",
            "https://techblog.woowahan.com/feed/",
            "https://techblog.yogiyo.co.kr/feed",
            "https://toss.tech/rss.xml",
            "https://yozm.wishket.com/magazine/list/develop/feed/",
            "https://techtopic.skplanet.com/rss.xml",
        ]
    )
    fun fetch(url: String) {
        assertDoesNotThrow {
            val xml = rssFeedFetcher.fetchXmlString(url)
            xmlParser.convertToArticles(xml)
        }
    }
}