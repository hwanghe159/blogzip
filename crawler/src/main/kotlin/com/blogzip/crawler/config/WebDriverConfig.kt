package com.blogzip.crawler.config

import com.blogzip.crawler.undetectedchromedriver.ChromeDriverBuilder
import io.github.bonigarcia.wdm.WebDriverManager
import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class WebDriverConfig(
    private val seleniumProperties: SeleniumProperties,
) {

    @Bean
    fun webDriverManager(): WebDriverManager {
        val webDriverManager = WebDriverManager.chromedriver()
        webDriverManager.setup()
        return webDriverManager
    }

    @Bean
    fun webDriver(): WebDriver {
//        val chromeOptions = ChromeOptions()
//        chromeOptions.addArguments(seleniumProperties.chromeOptions)
//        chromeOptions.addArguments()
//        val driver = ChromeDriver(chromeOptions)
//        driver.manage().timeouts().pageLoadTimeout(TIMEOUT)
//        return driver

//        val driver = ChromeDriver(ChromeOptions())
//        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(10))

        val driverHome = webDriverManager().downloadedDriverPath
        val chromeOptions = ChromeOptions()
        chromeOptions.addArguments(seleniumProperties.chromeOptions)
        return ChromeDriverBuilder()
            .build(chromeOptions, driverHome)
    }

    fun quitWebDriver() {
        webDriver().quit()
    }
}
