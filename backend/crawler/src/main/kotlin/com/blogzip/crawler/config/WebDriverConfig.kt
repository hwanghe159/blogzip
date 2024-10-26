package com.blogzip.crawler.config

import com.blogzip.crawler.undetectedchromedriver.ChromeDriverBuilder
import io.github.bonigarcia.wdm.WebDriverManager
import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeOptions

class WebDriverConfig(
    private val seleniumProperties: SeleniumProperties,
) {

    fun webDriver(): WebDriver {
//        val chromeOptions = ChromeOptions()
//        chromeOptions.addArguments(seleniumProperties.chromeOptions)
//        chromeOptions.addArguments("user-agent=Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.0.0 Safari/537.36")
//        val driver = ChromeDriver(chromeOptions)
//        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(100))
//        return driver

        val driverHome = webDriverManager().downloadedDriverPath
        val chromeOptions = ChromeOptions()
        chromeOptions.addArguments(seleniumProperties.chromeOptions)
        return ChromeDriverBuilder()
            .build(chromeOptions, driverHome)
    }

    private fun webDriverManager(): WebDriverManager {
        val webDriverManager = WebDriverManager.chromedriver()
        webDriverManager.setup()
        return webDriverManager
    }
}
