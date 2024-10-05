package com.blogzip.crawler.config

import io.github.bonigarcia.wdm.WebDriverManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class WebDriverManagerConfig {

    @Bean
    fun webDriverManager(): WebDriverManager {
        val webDriverManager = WebDriverManager.chromedriver()
        webDriverManager.setup()
        return webDriverManager
    }
}
