package com.blogzip.api.controller

import com.blogzip.api.common.logger
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.lang.RuntimeException

@RestController
class TestController {

    val log = logger()

    @GetMapping("/api/v1/test/error-log/message")
    fun errorMessage() {
        log.error("에러발생")
    }

    @GetMapping("/api/v1/test/error-log/exception")
    fun errorException() {
        val e = RuntimeException("에러발생")
        throw e
    }
}