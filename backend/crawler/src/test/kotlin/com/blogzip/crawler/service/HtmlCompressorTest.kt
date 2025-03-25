package com.blogzip.crawler.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class HtmlCompressorTest {

  @Test
  fun `불필요한 정보를 제거하고 md 형식으로 변환한다`() {
    val htmlCompressor = HtmlCompressor()
    val result = htmlCompressor.compress(
      """
                <h1>제목</h1>
                <ol style="border-bottom:none;border-left:none;border-right:none;border-top:none;">
                    <li>내용</li>
                    <li>내용</li>
                    <li>내용</li>
                </ol>
                <ul>
                    <li>내용</li>
                    <li>내용</li>
                    <li>내용</li>
                </ul>
                <script></script>
            """.trimIndent()
    )
    assertThat(result).isEqualTo(
      """
              제목
              ===
              
              1. 내용
              2. 내용
              3. 내용
              
              * 내용
              * 내용
              * 내용
              
        """.trimIndent()
    )
  }
}