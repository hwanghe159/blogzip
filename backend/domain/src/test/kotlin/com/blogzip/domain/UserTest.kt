package com.blogzip.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.time.DayOfWeek
import java.time.DayOfWeek.*
import java.time.LocalDate
import java.util.stream.Stream

class UserTest {

  @ParameterizedTest
  @MethodSource("dateProvider")
  fun `수신희망 요일에 기반하여 누적된 날짜를 반환한다`(
    receiveDays: List<DayOfWeek>,
    date: LocalDate,
    expected: List<LocalDate>
  ) {
    val user = User(
      email = "",
      socialType = SocialType.GOOGLE,
      socialId = "",
      receiveDays = ReceiveDaysConverter.toString(receiveDays),
    )

    val accumulatedDates = user.getAccumulatedDates(date)

    assertThat(accumulatedDates).isEqualTo(expected)
  }

  companion object {
    @JvmStatic
    fun dateProvider(): Stream<Arguments> {
      return Stream.of(
        Arguments.of(
          listOf(MONDAY, WEDNESDAY, FRIDAY), // 월수금
          LocalDate.of(2024, 5, 8), // 수요일
          listOf(LocalDate.of(2024, 5, 6), LocalDate.of(2024, 5, 7))
        ),
        Arguments.of(
          DayOfWeek.entries, // 월화수목금토일
          LocalDate.of(2024, 5, 8), // 수요일
          listOf(LocalDate.of(2024, 5, 7))
        ),
        Arguments.of(
          listOf(WEDNESDAY), // 수
          LocalDate.of(2024, 5, 8), // 수요일
          listOf(
            LocalDate.of(2024, 5, 1),
            LocalDate.of(2024, 5, 2),
            LocalDate.of(2024, 5, 3),
            LocalDate.of(2024, 5, 4),
            LocalDate.of(2024, 5, 5),
            LocalDate.of(2024, 5, 6),
            LocalDate.of(2024, 5, 7)
          )
        ),
      )
    }
  }
}