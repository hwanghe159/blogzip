package com.blogzip.service

import com.blogzip.common.DomainException
import com.blogzip.common.ErrorCode
import com.blogzip.domain.ReceiveDaysConverter
import com.blogzip.domain.SocialType
import com.blogzip.domain.User
import com.blogzip.domain.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.DayOfWeek

@Service
class UserService(private val repository: UserRepository) {

  @Transactional(readOnly = true)
  fun findAll(): List<User> {
    return repository.findAllByIsDeletedFalse()
  }

  @Transactional(readOnly = true)
  fun findById(id: Long): User? {
    return repository.findByIdAndIsDeletedFalse(id)
  }

  @Transactional(readOnly = true)
  fun findByEmail(email: String): User? {
    return repository.findByEmailAndIsDeletedFalse(email)
  }

  @Transactional(readOnly = true)
  fun findByEmailIncludingDeleted(email: String): User? {
    return repository.findByEmail(email)
  }

  @Transactional(readOnly = true)
  fun findAllByDayOfWeek(day: DayOfWeek): List<User> {
    return repository.findAllByDayOfWeek(day)
  }

  @Transactional
  fun save(user: User): User {
    return repository.save(user)
  }

  @Transactional(readOnly = true)
  fun findByGoogleId(googleId: String): User? {
    return repository.findBySocialTypeAndSocialIdAndIsDeletedFalse(SocialType.GOOGLE, googleId)
  }

  @Transactional
  fun update(id: Long, receiveDays: List<String>): User {
    return repository.findByIdAndIsDeletedFalse(id)
      ?.updateReceiveDays(ReceiveDaysConverter.toString(receiveDays.map { DayOfWeek.valueOf(it) }))
      ?: throw DomainException(ErrorCode.USER_NOT_FOUND)
  }

  @Transactional
  fun withdraw(id: Long) {
    val user = repository.findById(id)
      .orElseThrow { DomainException(ErrorCode.USER_NOT_FOUND) }
    if (user.isDeleted) {
      throw DomainException(ErrorCode.USER_WITHDRAWN)
    }
    user.withdraw()
  }
}
