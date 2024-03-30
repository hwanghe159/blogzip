package com.blogzip.api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["com.blogzip"])
class ApiApplication

fun main(args: Array<String>) {
//    System.setProperty("http://apache.org/xml/features/disallow-doctype-decl", "false")
    runApplication<ApiApplication>(*args)
}
