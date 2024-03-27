package com.blogzip.api.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class SampleController {

    @GetMapping("/api/v1/test")
    fun test(): String {
        return "안녕하세요"
    }
}