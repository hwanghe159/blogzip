package com.blogzip.api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class BlogzipApplication

fun main(args: Array<String>) {
    runApplication<BlogzipApplication>(*args)
}
