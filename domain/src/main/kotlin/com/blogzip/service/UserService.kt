package com.blogzip.service

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
}