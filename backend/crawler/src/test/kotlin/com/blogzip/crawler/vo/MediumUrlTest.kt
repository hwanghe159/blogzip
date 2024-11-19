package com.blogzip.crawler.vo

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class MediumUrlTest {

    @DisplayName("medium 링크에서 rss 링크로 변환한다")
    @ParameterizedTest
    @CsvSource(
        delimiterString = " -> ",
        textBlock = """
https://medium.com/riiid-teamblog-kr/tagged/engineering -> https://medium.com/feed/riiid-teamblog-kr/tagged/engineering
https://medium.com/daangn -> https://medium.com/feed/daangn
"""
    )
    fun rssUrl(mediumUrl: String, expected: String) {
        assertThat(MediumUrl(mediumUrl).rssUrl()).isEqualTo(expected)
    }

}