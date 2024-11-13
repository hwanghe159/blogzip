package com.blogzip.ai

import com.blogzip.ai.summary.SingleRequestGenerator
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName

import org.junit.jupiter.api.Test

class SingleRequestGeneratorTest {

    private val generator = SingleRequestGenerator()

    @DisplayName("JSON 변환 테스트")
    @Test
    fun generate() {
        val request = generator.generate("1", "내용")

        assertThat(request.customId).isEqualTo("1")
        assertThat(request.body.messages[1].content).isEqualTo("내용")
    }
}