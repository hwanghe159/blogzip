package com.blogzip.batch

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import kotlin.system.exitProcess

@SpringBootApplication(scanBasePackages = ["com.blogzip"])
class BatchApplication

fun main(args: Array<String>) {
    val context = runApplication<BatchApplication>(*args)
    exitProcess(SpringApplication.exit(context))
}
