package com.blogzip.crawler.config

import io.github.bonigarcia.wdm.WebDriverManager


class WebDriverInitializer {

    companion object {
        fun initialize() {
            WebDriverManager.chromedriver().setup()
        }
    }
}