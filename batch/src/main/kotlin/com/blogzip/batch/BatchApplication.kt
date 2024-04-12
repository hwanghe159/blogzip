package com.blogzip.batch

import com.blogzip.crawler.confg.WebDriverInitializer
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import kotlin.system.exitProcess

@SpringBootApplication(scanBasePackages = ["com.blogzip"])
class BatchApplication

fun main(args: Array<String>) {
    WebDriverInitializer.initialize()
    val context = runApplication<BatchApplication>(*args)
    exitProcess(SpringApplication.exit(context))
}
