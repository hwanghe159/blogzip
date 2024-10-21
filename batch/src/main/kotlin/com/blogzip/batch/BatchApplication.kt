package com.blogzip.batch

import com.blogzip.crawler.config.WebDriverConfig
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import kotlin.system.exitProcess

@SpringBootApplication(scanBasePackages = ["com.blogzip"])
class BatchApplication

fun main(args: Array<String>) {
    val context = runApplication<BatchApplication>(*args)
    context.getBean(WebDriverConfig::class.java).quitWebDriver()
    exitProcess(SpringApplication.exit(context))
}
