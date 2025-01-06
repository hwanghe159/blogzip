package com.blogzip.crawler.service

import com.blogzip.crawler.config.SeleniumProperties
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.BeforeEach

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.web.reactive.function.client.WebClient

class BlogMetadataScrapperTest {

    private lateinit var jsoupBlogMetadataScrapper: JsoupBlogMetadataScrapper
    private lateinit var seleniumBlogMetadataScrapper: SeleniumBlogMetadataScrapper

    @BeforeEach
    fun setUp() {
        this.jsoupBlogMetadataScrapper =
            JsoupBlogMetadataScrapper(RssFeedFetcher.create())
        this.seleniumBlogMetadataScrapper =
            SeleniumBlogMetadataScrapper.create(SeleniumProperties(listOf("--window-size=1920,1080")))
    }

    @AfterEach
    fun tearDown() {
        seleniumBlogMetadataScrapper.endUse()
    }

    // todo jsoup으로는 동적 웹페이지의 메타데이터를 가져올 수 없는 경우가 있는듯. 빠르면서 확실히 가져올 수 있는 방법 강구하기
    @ParameterizedTest
    @CsvSource(
        value = [
            "https://techblog.lycorp.co.jp/ko",
            "https://medium.com/coupang-engineering/kr/home",
            "https://danawalab.github.io",
            "https://42dot.ai/blog",
            "https://medium.com/tving-team/tech/home",
//            "https://medium.com/daangn",
//            "https://medium.com/watcha",
//            "https://medium.com/29cm",
//            "https://medium.com/wantedjobs",
//            "https://medium.com/cj-onstyle",
//            "https://medium.com/platfarm",
//            "https://medium.com/musinsa-tech",
//            "https://medium.com/@greg.shiny82",
//            "https://medium.com/naverfinancial",
//            "https://medium.com/riiid-teamblog-kr/tagged/engineering",
//            "https://medium.com/naver-place-dev",
//            "https://techblog.woowahan.com",
//            "https://d2.naver.com/helloworld",
//            "https://hyperconnect.github.io",
//            "https://blog.banksalad.com/tech",
//            "https://tech.socarcorp.kr",
//            "https://techblog.yogiyo.co.kr",
//            "https://toss.tech",
//            "https://techblog.gccompany.co.kr",
//            "https://meetup.nhncloud.com",
//            "https://tech.kakao.com",
//            "http://thefarmersfront.github.io",
//            "https://jojoldu.tistory.com",
//            "https://medium.com/zigbang",
//            "https://saramin.github.io",
//            "https://smilegate.ai",
//            "https://techtopic.skplanet.com",
//            "https://tech.kakaopay.com",
//            "https://dev.gmarket.com",
//            "https://techblog.lotteon.com",
//            "https://engineering.ab180.co",
//            "https://techblog.tabling.co.kr",
//            "https://spoqa.github.io",
//            "https://netmarble.engineering",
//            "https://yozm.wishket.com/magazine/list/develop",
//            "https://devocean.sk.com/tech",
//            "https://blog.gangnamunni.com/blog",
//            "https://blog.est.ai",
//            "https://snack.planetarium.dev/kor",
//            "https://ridicorp.com/story-category/tech-blog",
//            "https://blog.hwahae.co.kr/category/all/tech",
//            "https://oliveyoung.tech/blog",
//            "https://tech.inflab.com",
//            "https://blog.wadiz.kr/archive/tech-story/",
//            "https://bucketplace.com/culture/Tech/",
//            "https://tech.devsisters.com",
//            "https://channel.io/ko/blog/topics/716ee7b4",
//            "https://devblog.kakaostyle.com/ko/",
//            "https://junuuu.tistory.com",
//            "https://mangkyu.tistory.com",
//            "https://tech.kakaoenterprise.com/category/Tech%20Log",
//            "https://blog.dramancompany.com",
//            "https://velog.io/@doogang",
//            "https://tecoble.techcourse.co.kr",
//            "https://f-lab.kr/blog?category=Tech",
//            "https://tech.kakaobank.com",
        ]
    )
    fun `jsoup을 사용한 메타데이터 추출 결과가 selenium을 사용한 메타데이터 추출 결과보다 자세해야 한다`(url: String) {
        val jsoupStart = System.currentTimeMillis()
        val jsoupMetadata = jsoupBlogMetadataScrapper.getMetadata(url)
        val jsoupEnd = System.currentTimeMillis()
        println("jsoup 소요시간: ${jsoupEnd - jsoupStart} ms")

        val seleniumStart = System.currentTimeMillis()
        val seleniumMetadata = seleniumBlogMetadataScrapper.getMetadata(url)
        val seleniumEnd = System.currentTimeMillis()
        println("selenium 소요시간: ${seleniumEnd - seleniumStart} ms")

        assertAll(
            Executable {
                assertThat(jsoupMetadata.title.replace("-", "–")).isEqualTo(seleniumMetadata.title)
            },
            Executable {
                if (seleniumMetadata.imageUrl != null) {
                    if (jsoupMetadata.imageUrl == null) {
                        throw AssertionError("selenium은 imageUrl 추출 가능, jsoup은 imageUrl 추출 불가")
                    } else {
                        assertThat(jsoupMetadata.imageUrl).isEqualTo(seleniumMetadata.imageUrl)
                    }
                }
            },
            Executable {
                if (seleniumMetadata.rss != null) {
                    if (jsoupMetadata.rss == null) {
                        throw AssertionError("selenium은 rss 추출 가능, jsoup은 rss 추출 불가")
                    } else {
                        assertThat(jsoupMetadata.rss).isEqualTo(seleniumMetadata.rss)
                    }
                }
            },
        )
    }
}