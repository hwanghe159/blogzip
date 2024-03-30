package com.blogzip.batch

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["com.blogzip"])
class BatchApplication

fun main(args: Array<String>) {
    runApplication<BatchApplication>(*args)
}
