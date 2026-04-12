package com.blogzip.service

import com.blogzip.common.DomainException
import com.blogzip.common.ErrorCode
import com.blogzip.domain.ReadLaterRepository
import com.blogzip.domain.SocialType
import com.blogzip.domain.User
import com.blogzip.domain.UserRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoSettings
import java.util.Optional

@MockitoSettings
class UserServiceTest {

  @Mock
  lateinit var userRepository: UserRepository

  @Mock
  lateinit var readLaterRepository: ReadLaterRepository

  @InjectMocks
  lateinit var userService: UserService

  @DisplayName("활성 회원 조회는 같은 이메일 중 최신 사용자 1건만 반환한다.")
  @Test
  fun findByEmail() {
    val user = User(
      id = 2L,
      email = "test@blogzip.com",
      socialType = SocialType.GOOGLE,
      socialId = "google-2",
      receiveDays = "MONDAY",
      isDeleted = false,
    )
    `when`(userRepository.findFirstByEmailAndIsDeletedOrderByIdDesc("test@blogzip.com", false))
      .thenReturn(user)

    val found = userService.findByEmail("test@blogzip.com")

    assertSame(user, found)
    verify(userRepository).findFirstByEmailAndIsDeletedOrderByIdDesc("test@blogzip.com", false)
  }

  @DisplayName("전체 회원 조회는 활성 회원 우선으로 같은 이메일 중 1건만 반환한다.")
  @Test
  fun findByEmailIncludingDeleted() {
    val user = User(
      id = 2L,
      email = "test@blogzip.com",
      socialType = SocialType.GOOGLE,
      socialId = "google-2",
      receiveDays = "MONDAY",
      isDeleted = false,
    )
    `when`(userRepository.findFirstByEmailOrderByIsDeletedAscIdDesc("test@blogzip.com"))
      .thenReturn(user)

    val found = userService.findByEmailIncludingDeleted("test@blogzip.com")

    assertSame(user, found)
    verify(userRepository).findFirstByEmailOrderByIsDeletedAscIdDesc("test@blogzip.com")
  }

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
    user.addSubscription(11L)
    `when`(userRepository.findById(1L))
      .thenReturn(Optional.of(user))

    userService.withdraw(1L)

    verify(readLaterRepository).deleteAllByUserId(1L)
    assertTrue(user.isDeleted)
    assertNotNull(user.deletedAt)
    assertTrue(user.subscriptions.isEmpty())
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
