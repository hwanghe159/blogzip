package com.blogzip.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class BlogUrlTest {

    @DisplayName("블로그 URL 동등성 판단")
    @Test
    fun equals() {
        val blogUrls = setOf(
            BlogUrl.from("naver.com"),
            BlogUrl.from("naver.com/"),
            BlogUrl.from("https://naver.com"),
            BlogUrl.from("https://naver.com/"),
            BlogUrl.from("https://www.naver.com"),
            BlogUrl.from("https://www.naver.com/"),
            BlogUrl.from("www.naver.com"),
            BlogUrl.from("www.naver.com/"),
            BlogUrl.from("www.naver.com:443"),
        )

        assertThat(blogUrls).hasSize(1)
        blogUrls.forEach { url -> assertThat(url.toString()).isEqualTo("https://naver.com") }
    }


    @Test
    fun name() {
        val blogs = listOf(
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
            "https://spoqa.github.io",
            "https://netmarble.engineering",
            "https://yozm.wishket.com/magazine/list/develop",
            "https://devocean.sk.com/tech",
            "https://blog.gangnamunni.com/blog",
            "https://blog.est.ai",
            "https://medium.com/platfarm",
            "https://snack.planetarium.dev/kor",
            "https://ridicorp.com/story-category/tech-blog",
            "https://medium.com/musinsa-tech",
            "https://medium.com/@greg.shiny82",
            "https://medium.com/naverfinancial",
            "https://blog.hwahae.co.kr/category/all/tech",
            "https://oliveyoung.tech/blog",
        )
        blogs.forEach {
            println("$it -> ${BlogUrl.from(it)}")
        }
    }
}