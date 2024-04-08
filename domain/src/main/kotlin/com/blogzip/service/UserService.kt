package com.blogzip.service

import com.blogzip.common.DomainException
import com.blogzip.common.ErrorCode
import com.blogzip.domain.User
import com.blogzip.domain.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(private val repository: UserRepository) {

    @Transactional(readOnly = true)
    fun findAll(): List<User> {
        return repository.findAll()
    }

    @Transactional(readOnly = true)
    fun findByEmail(email: String): User? {
        return repository.findByEmail(email)
    }

    @Transactional
    fun verify(email: String, code: String) {
        val user = repository.findByEmail(email) ?: throw DomainException(ErrorCode.EMAIL_NOT_FOUND)
        user.verify(code)
    }

    @Transactional
    fun save(
        email: String,
        verificationCode: String
    ): User {
        return repository.save(User(email = email, verificationCode = verificationCode))
    }
}