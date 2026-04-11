package com.blogzip.service

import com.blogzip.common.DomainException
import com.blogzip.common.ErrorCode
import com.blogzip.domain.SocialType
import com.blogzip.domain.User
import com.blogzip.domain.UserRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoSettings
import java.util.Optional

@MockitoSettings
class UserServiceTest {

  @Mock
  lateinit var userRepository: UserRepository

  @InjectMocks
  lateinit var userService: UserService

  @DisplayName("회원 탈퇴 시 user에 탈퇴 플래그가 반영된다.")
  @Test
  fun withdraw() {
    val user = User(
      id = 1L,
      email = "test@blogzip.com",
      socialType = SocialType.GOOGLE,
      socialId = "google-1",
      receiveDays = "MONDAY",
      isDeleted = false,
    )
    `when`(userRepository.findById(1L))
      .thenReturn(Optional.of(user))

    userService.withdraw(1L)

    assertTrue(user.isDeleted)
    assertNotNull(user.deletedAt)
  }

  @DisplayName("이미 탈퇴한 회원은 다시 탈퇴할 수 없다.")
  @Test
  fun withdraw_FailWhenAlreadyWithdrawn() {
    val user = User(
      id = 1L,
      email = "test@blogzip.com",
      socialType = SocialType.GOOGLE,
      socialId = "google-1",
      receiveDays = "MONDAY",
      isDeleted = true,
    )
    `when`(userRepository.findById(1L))
      .thenReturn(Optional.of(user))

    val exception = assertThrows(DomainException::class.java) {
      userService.withdraw(1L)
    }

    assertEquals(ErrorCode.USER_WITHDRAWN, exception.errorCode)
  }
}
