package com.blogzip.crawler.service

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import org.springframework.stereotype.Component
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.time.Duration

@Component
class WebScrapper(private val htmlCompressor: HtmlCompressor) {

    companion object {
        private val TIMEOUT = Duration.ofSeconds(10)
        private val RSS_POSTFIX = listOf("/rss", "/feed", "/rss.xml", "/feed.xml")
    }

    fun getTitle(url: String): String {
        val webDriver = createWebDriver()
        webDriver.get(url)
        val wait = WebDriverWait(webDriver, TIMEOUT)
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.tagName("title"), ""))
        val pageTitle: String = webDriver.title
        webDriver.quit()
        return pageTitle
    }

    fun convertToRss(url: String): List<String> {
        val webDriver = createWebDriver()
        webDriver.get(url)
        val wait = WebDriverWait(webDriver, TIMEOUT)
        wait.until(ExpectedConditions.jsReturnsValue("return document.readyState == 'complete';"))
        val links: List<WebElement> = webDriver.findElements(
            By.cssSelector("link[type='application/rss+xml'], link[type='application/atom+xml']")
        )
        val result = links.map { it.getAttribute("href") }
        webDriver.quit()
        if (result.isNotEmpty()) {
            return result
        }

        val url2 = URI.create(url).toURL()
        if (url2.host == "velog.io") {
            val userName = url2.path.substringAfterLast("@").split("/")[0]
            return listOf("https://v2.velog.io/rss/${userName}")
        }

        var rssUrl: String? = null
        for (postfix in RSS_POSTFIX) {
            val connection = URL(url + postfix).openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            val contentType = connection.contentType ?: break
            if (contentType.startsWith("application/rss+xml") ||
                contentType.startsWith("application/atom+xml") ||
                contentType.startsWith("text/xml")
            ) {
                rssUrl = url + postfix
                break
            }
        }
        return if (rssUrl != null) {
            listOf(rssUrl)
        } else {
            emptyList()
        }
    }

    fun getContent(url: String): String {
        val webDriver = createWebDriver()
        webDriver.get(url)
        val wait = WebDriverWait(webDriver, TIMEOUT)
        wait.until(ExpectedConditions.jsReturnsValue("return document.readyState == 'complete';"))
        val content: String = webDriver.pageSource
        webDriver.quit()
        return htmlCompressor.compress(content)
    }

    private fun createWebDriver(): WebDriver {
        val chromeOptions = ChromeOptions()
        chromeOptions.addArguments("--headless", "--no-sandbox")
        return ChromeDriver(chromeOptions)
    }
}