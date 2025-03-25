package com.blogzip.ai

import com.blogzip.ai.summary.SingleRequestGenerator
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SingleRequestGeneratorTest {

  private val generator = SingleRequestGenerator()

  @Test
  fun `SingleRequest 인스턴스 생성`() {
    val request = generator.generate("1", "내용", "model")

    assertThat(request.customId).isEqualTo("1")
    assertThat(request.body.messages[1].content).isEqualTo("내용")
    assertThat(request.body.model).isEqualTo("model")
  }
}