package com.blogzip.crawler.confg

import io.github.bonigarcia.wdm.WebDriverManager


class WebDriverInitializer {

    companion object {
        fun initialize() {
            WebDriverManager.chromedriver().driverVersion("123.0.6312.122").setup()
        }
    }
}