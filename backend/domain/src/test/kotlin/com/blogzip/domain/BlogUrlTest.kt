package com.blogzip.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.net.URISyntaxException

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

    @ParameterizedTest
    @CsvSource(
        delimiterString = " -> ",
        textBlock = """
https://f-lab.kr/blog?category=Tech -> https://f-lab.kr/blog?category=Tech
naver.com -> https://naver.com
"""
    )
    fun `toString 테스트`(input: String, expected: String) {
        assertThat(BlogUrl.from(input).toString()).isEqualTo(expected)
    }

    @ParameterizedTest
    @CsvSource(value = ["aa", "올바르지않은 url"])
    fun `예외 발생 테스트`(url: String) {
        assertThrows<URISyntaxException> {
            BlogUrl.from(url)
        }
    }
}