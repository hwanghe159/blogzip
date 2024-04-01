package com.blogzip.api.dto

import java.security.SecureRandom

data class RandomCode(private val length: Int) {

    val value: String

    init {
        val characters = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
        val random = SecureRandom()
        value = (1..length)
            .map { characters[random.nextInt(characters.length)] }
            .joinToString("")
    }
}