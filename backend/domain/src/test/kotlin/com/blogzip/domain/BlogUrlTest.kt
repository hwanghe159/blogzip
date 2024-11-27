package com.blogzip.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class BlogUrlTest {

    @Test
    fun `블로그 URL 동등성 판단`() {
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
}