package com.blogzip.api

import com.blogzip.crawler.confg.WebDriverInitializer
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["com.blogzip"])
class ApiApplication

fun main(args: Array<String>) {
    WebDriverInitializer.initialize()
    runApplication<ApiApplication>(*args)
}
