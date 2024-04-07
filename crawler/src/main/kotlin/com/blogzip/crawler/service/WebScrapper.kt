package com.blogzip.crawler.service

import com.blogzip.crawler.common.logger
import org.openqa.selenium.By
import org.openqa.selenium.TimeoutException
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.support.ui.ExpectedCondition
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import org.springframework.stereotype.Component
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.time.Duration

@Component
class WebScrapper(private val htmlCompressor: HtmlCompressor) {

    val log = logger()

    companion object {
        private val TIMEOUT = Duration.ofSeconds(20)
        private val RSS_POSTFIX = listOf("/rss", "/feed", "/rss.xml", "/feed.xml")
    }

    fun getTitle(url: String): String {
        val webDriver = createWebDriver()
        try {
            webDriver.get(url)
            val wait = WebDriverWait(webDriver, TIMEOUT)
            wait.until(ExpectedConditions.textToBePresentInElementLocated(By.tagName("title"), ""))
            val pageTitle: String = webDriver.title
            return pageTitle
        } catch (e: TimeoutException) {
            throw RuntimeException("웹페이지의 타이틀 조회 실패.")
        } finally {
            webDriver.quit()
        }
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


    fun getContent(url: String): String? {
        val webDriver = createWebDriver()
        try {
            webDriver.get(url)
            val wait = WebDriverWait(webDriver, TIMEOUT)
            wait.until(ExpectedConditions.jsReturnsValue("return document.readyState == 'complete';"))
            Thread.sleep(3000L) // 페이지 내용이 모두 로딩될때까지 기다린다
            val content: String = webDriver.pageSource
            return htmlCompressor.compress(content)
        } catch (e: Exception) {
            log.error("글 크롤링 실패. article.url=${url}", e)
            return null
        } finally {
            webDriver.quit()
        }
    }

    fun getArticles(blogUrl: String, cssSelector: String): List<ArticleData> {
        val webDriver = createWebDriver()
        try {
            webDriver.get(blogUrl)
            val wait = WebDriverWait(webDriver, TIMEOUT)
            wait.until(
                ExpectedCondition { driver: WebDriver ->
                    val element =
                        driver.findElement(By.cssSelector(cssSelector))
                    val href = element.getAttribute("href")
                    href != null && href.isNotEmpty()
                }
            )
            return webDriver.findElements(By.cssSelector(cssSelector))
                .map {
                    ArticleData(
                        title = it.text,
                        url = it.getAttribute("href")
                    )
                }
        } catch (e: Exception) {
            log.error("블로그 크롤링 실패. blog.url=${blogUrl}", e)
            return emptyList()
        } finally {
            webDriver.quit()
        }
    }

    private fun createWebDriver(): WebDriver {
        val chromeOptions = ChromeOptions()
        chromeOptions.addArguments("--headless", "--no-sandbox")
        val driver = ChromeDriver(chromeOptions)
        driver.manage().timeouts().pageLoadTimeout(TIMEOUT)
        return driver
    }

    data class ArticleData(
        val title: String,
        val url: String,
    )
}