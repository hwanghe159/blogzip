package com.blogzip.ai

import com.blogzip.ai.common.JsonlConverter
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class JsonlConverterTest {

  private val converter = JsonlConverter()

  @DisplayName("인스턴스 -> jsonl")
  @Test
  fun objectsToJsonl() {
    val objects = listOf(TestClass(a = 1, b = "hi"), TestClass(a = 2, b = "hello"))
    assertThat(converter.objectsToJsonl(objects)).isEqualTo(
      """
                {"a":1,"b":"hi"}
                {"a":2,"b":"hello"}
            """.trimIndent()
    )
  }

  @DisplayName("jsonl -> 인스턴스")
  @Test
  fun jsonlToObjects() {
    val jsonl = """
                {"a":1,"b":"hi"}
                {"a":2,"b":"hello"}
            """.trimIndent()
    val objects = converter.jsonlToObjects(jsonl, TestClass::class.java)

    assertThat(objects).hasSize(2)
    assertThat(objects[0]).isEqualTo(TestClass(a = 1, b = "hi"))
    assertThat(objects[1]).isEqualTo(TestClass(a = 2, b = "hello"))
  }

  data class TestClass(val a: Int, val b: String)
}