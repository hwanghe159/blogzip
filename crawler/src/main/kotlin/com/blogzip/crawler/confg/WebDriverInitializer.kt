package com.blogzip.crawler.confg

import io.github.bonigarcia.wdm.WebDriverManager


class WebDriverInitializer {

    companion object {
        fun initialize() {
            WebDriverManager.chromedriver().setup()
        }
    }
}