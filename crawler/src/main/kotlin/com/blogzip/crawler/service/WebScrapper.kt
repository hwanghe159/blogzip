package com.blogzip.crawler.service

import com.blogzip.crawler.common.logger
import com.blogzip.crawler.vo.VelogUrl
import org.openqa.selenium.*
import org.openqa.selenium.interactions.Actions
import org.openqa.selenium.support.ui.ExpectedCondition
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import java.time.Duration
import kotlin.random.Random

@Component
class WebScrapper(
    private val defaultWebClient: WebClient,
    private val htmlCompressor: HtmlCompressor,
    private val webDriver: WebDriver,
) {

    val log = logger()

    companion object {
        private val TIMEOUT = Duration.ofSeconds(100)
        private val RSS_POSTFIX = listOf("/rss", "/feed", "/rss.xml", "/feed.xml")
        private val RSS_CONTENT_TYPE =
            setOf("application/xml", "application/rss+xml", "application/atom+xml", "text/xml")
    }

    fun getTitle(url: String): String {
        try {
            webDriver.get(url)
            val wait = WebDriverWait(webDriver, TIMEOUT)
            wait.until(ExpectedConditions.textToBePresentInElementLocated(By.tagName("title"), ""))
            val pageTitle: String = webDriver.title
            return pageTitle
        } catch (e: Exception) {
            throw RuntimeException("웹페이지의 타이틀 조회 실패. url=$url", e)
        }
    }

    fun getImageUrl(url: String): String {
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
        }
    }

    fun convertToRss(url: String): List<String> {
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
            val containsRssUrl = defaultWebClient.get()
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
        }
    }

    /**
     * 아래는 모든 title 정보를 가져오는 js 코드
     * const articles = document.querySelectorAll('...');
     * const titles = Array.from(articles).map(article => article.textContent.trim())
     */
    fun getArticles(blogUrl: String, cssSelector: String): ScrapResult {
        try {
            webDriver.get(blogUrl)
            scrollToBottom()
            val wait = WebDriverWait(webDriver, TIMEOUT)
            wait.until(
                ExpectedCondition { driver ->
                    val elements =
                        driver.findElements(By.cssSelector(cssSelector))
                    elements.isNotEmpty() && elements.all { it.text.isNotBlank() }
                }
            )
            val articles = webDriver.findElements(By.cssSelector(cssSelector))
                .map { element ->
                    // todo 실패 시 블로그 내 모든 글 실패가 아닌 하나만 실패하도록
                    Thread.sleep(Random.nextLong(1000, 3001)) // 1~3초 sleep
                    val title = element.text.trim()
                    val currentWindow = webDriver.windowHandle
                    element.openNewTab(webDriver)
                    val newWindow = webDriver.windowHandles.lastOrNull { it != currentWindow }
                    webDriver.switchTo().window(newWindow)
                    val url = webDriver.currentUrl
                    webDriver.close()
                    webDriver.switchTo().window(currentWindow)
                    log.info("- 제목 : ${title}, url : ${url}")
                    Article(
                        title = title,
                        url = url
                    )
                }
            return ScrapResult(articles, null)
        } catch (e: Exception) {
            log.error("블로그 크롤링 실패. blog.url=${blogUrl}", e)
            return ScrapResult(emptyList(), e)
        } finally {
            initializeWebDriver()
        }
    }

    fun test(url: String): String? {
        try {
            webDriver.get(url)
            val pageSource = webDriver.pageSource
            initializeWebDriver()
            return pageSource
        } catch (e: Exception) {
            log.error(e.message, e)
        }
        return null
    }

    fun scrollToBottom() {
        val jsExecutor = webDriver as JavascriptExecutor
        jsExecutor.executeScript("window.scrollTo(0, document.body.scrollHeight);")
    }

    fun initializeWebDriver() {
        webDriver.get("chrome://newtab")
    }

    data class Article(
        val title: String,
        val url: String,
    )

    data class ScrapResult(
        val articles: List<Article>,
        val failCause: Exception?,
    )
}

private fun WebElement.openNewTab(webDriver: WebDriver) {
    val actions = Actions(webDriver)
    val modifierKey =
        if (System.getProperty("os.name").contains("Mac")) Keys.COMMAND else Keys.CONTROL
    actions.keyDown(modifierKey).click(this).keyUp(modifierKey).build().perform()
}
