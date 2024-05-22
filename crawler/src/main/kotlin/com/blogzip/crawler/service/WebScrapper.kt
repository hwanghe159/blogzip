package com.blogzip.crawler.service

import com.blogzip.crawler.common.logger
import com.blogzip.crawler.confg.SeleniumProperties
import com.blogzip.crawler.vo.VelogUrl
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.support.ui.ExpectedCondition
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import java.time.Duration

@Component
class WebScrapper(
    private val webClient: WebClient,
    private val htmlCompressor: HtmlCompressor,
    private val seleniumProperties: SeleniumProperties,
) {

    val log = logger()

    companion object {
        private val TIMEOUT = Duration.ofSeconds(100)
        private val RSS_POSTFIX = listOf("/rss", "/feed", "/rss.xml", "/feed.xml")
        private val RSS_CONTENT_TYPE =
            setOf("application/xml", "application/rss+xml", "application/atom+xml", "text/xml")
    }

    fun getTitle(url: String): String {
        val webDriver = createWebDriver()
        try {
            webDriver.get(url)
            val wait = WebDriverWait(webDriver, TIMEOUT)
            wait.until(ExpectedConditions.textToBePresentInElementLocated(By.tagName("title"), ""))
            val pageTitle: String = webDriver.title
            return pageTitle
        } catch (e: Exception) {
            throw RuntimeException("웹페이지의 타이틀 조회 실패. url=$url", e)
        } finally {
            webDriver.quit()
        }
    }

    fun getImageUrl(url: String): String {
        val webDriver = createWebDriver()
        try {
            webDriver.get(url)
            val wait = WebDriverWait(webDriver, TIMEOUT)
            val element =
                wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("meta[property='og:image']")))
            val imageUrl = element.getAttribute("content")
            if (imageUrl.isBlank()) {
                throw RuntimeException("head 태그 내 image가 공백임.")
            }
            return imageUrl
        } catch (e: Exception) {
            throw RuntimeException("웹페이지의 이미지 조회 실패. url=$url", e)
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

        if (VelogUrl.isVelogUrl(url)) {
            return listOf(VelogUrl(url).rssUrl())
        }

        var rssUrl: String? = null
        for (postfix in RSS_POSTFIX) {
            val containsRssUrl = webClient.get()
                .uri(url + postfix)
                .retrieve()
                .toEntity(String::class.java)
                .map { response ->
                    RSS_CONTENT_TYPE.contains(response.headers.contentType.toString())
                }
                .onErrorReturn(false)
                .block() ?: false
            if (containsRssUrl) {
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
        // js 코드 -> document.querySelectorAll('...');
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
        chromeOptions.addArguments(seleniumProperties.chromeOptions)
        val driver = ChromeDriver(chromeOptions)
        driver.manage().timeouts().pageLoadTimeout(TIMEOUT)
        return driver
    }

    data class ArticleData(
        val title: String,
        val url: String,
    )
}