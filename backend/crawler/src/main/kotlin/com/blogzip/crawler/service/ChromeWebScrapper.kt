package com.blogzip.crawler.service

import com.blogzip.crawler.config.SeleniumProperties
import com.blogzip.crawler.service.WebScrapper.*
import com.blogzip.crawler.undetectedchromedriver.ChromeDriverBuilder
import com.blogzip.crawler.vo.MediumUrl
import com.blogzip.logger
import com.blogzip.crawler.vo.VelogUrl
import io.github.bonigarcia.wdm.WebDriverManager
import org.openqa.selenium.*
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.interactions.Actions
import org.openqa.selenium.support.ui.ExpectedCondition
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient
import java.net.URI
import java.time.Duration
import kotlin.random.Random

class ChromeWebScrapper private constructor(
    private val webClient: WebClient,
    private val htmlCompressor: HtmlCompressor,
    private val webDriver: WebDriver,
) : WebScrapper {

    val log = logger()

    companion object {
        private val BATCH_TIMEOUT = Duration.ofSeconds(100)
        private val CLIENT_TIMEOUT = Duration.ofSeconds(5)
        private val RSS_POSTFIX = listOf("/rss", "/feed", "/rss.xml", "/feed.xml")
        private val RSS_CONTENT_TYPE =
            setOf("application/xml", "application/rss+xml", "application/atom+xml", "text/xml")

        fun create(properties: SeleniumProperties): ChromeWebScrapper {
            val webDriverManager = WebDriverManager.chromedriver()
            webDriverManager.setup()
            val driverHome = webDriverManager.downloadedDriverPath
            val chromeOptions = ChromeOptions()
            chromeOptions.addArguments(properties.chromeOptions)
            val webDriver = ChromeDriverBuilder()
                .build(chromeOptions, driverHome)

            return ChromeWebScrapper(
                WebClient.builder()
                    .exchangeStrategies(
                        ExchangeStrategies.builder()
                            .codecs { it.defaultCodecs().maxInMemorySize(-1) }
                            .build()
                    ).build(),
                HtmlCompressor(),
                webDriver
            )
        }
    }

    @Synchronized
    override fun getMetadata(url: String): BlogMetadata {
        var title: String? = null
        var imageUrl: String? = null
        var rss: String? = null
        try {
            // title
            webDriver.get(url)
            val wait = WebDriverWait(webDriver, CLIENT_TIMEOUT)

            wait.until(
                ExpectedConditions.textToBePresentInElementLocated(By.tagName("title"), "")
            )
            title = webDriver.title

            // imageUrl
            try {
                val element =
                    wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("meta[property='og:image']")))
                imageUrl = element.getAttribute("content")
            } catch (e: Exception) {
                imageUrl = null
            }

            // 상대경로인 경우 절대경로로 변경
            if (imageUrl != null && !imageUrl.startsWith("http")) {
                imageUrl = URI(webDriver.currentUrl).resolve(imageUrl).toString()
            }

            // rss
            wait.until(ExpectedConditions.jsReturnsValue("return document.readyState == 'complete';"))
            val links: List<WebElement> = webDriver.findElements(
                By.cssSelector("link[type='application/rss+xml'], link[type='application/atom+xml']")
            )
            val result = links.map { it.getAttribute("href") }
            if (result.isNotEmpty()) {
                rss = result[0]
            } else if (VelogUrl.isVelogUrl(url)) {
                rss = VelogUrl(url).rssUrl()
            } else if (MediumUrl.isMediumUrl(url)) {
                rss = MediumUrl(url).rssUrl()
            } else {
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
                        rss = url + postfix
                        break
                    }
                }
            }
            return BlogMetadata(title = title, imageUrl = imageUrl, rss = rss)
        } catch (e: Exception) {
            val metadata = BlogMetadata(title = title!!, imageUrl = imageUrl, rss = rss)
            log.error("${url}에서 메타데이터 추출 중 예외 발생. metadata=${metadata}", e)
            return metadata
        } finally {
            initializeWebDriver()
        }
    }

    override fun getContent(url: String): String? {
        try {
            webDriver.get(url)
            val wait = WebDriverWait(webDriver, BATCH_TIMEOUT)
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
    override fun getArticles(
        blogUrl: String,
        cssSelector: String,
        articleUrls: Set<String>
    ): ScrapResult {
        val articles = mutableListOf<Article>()
        try {
            webDriver.get(blogUrl)
            scrollToBottom()
            val wait = WebDriverWait(webDriver, BATCH_TIMEOUT)
            wait.until(
                ExpectedCondition { driver ->
                    val elements =
                        driver.findElements(By.cssSelector(cssSelector))
                    elements.isNotEmpty() && elements.all { it.text.isNotBlank() }
                }
            )

            for (element in webDriver.findElements(By.cssSelector(cssSelector))) {
                Thread.sleep(Random.nextLong(1000, 3001)) // 1~3초 sleep
                val title = element.text.trim()

                // 새 탭으로 열기
                val currentWindow = webDriver.windowHandle
                element.openNewTab(webDriver)
                val newWindow = webDriver.windowHandles.lastOrNull { it != currentWindow }
                webDriver.switchTo().window(newWindow)

                // 새 탭의 정보 추출
                Thread.sleep(Random.nextLong(3000)) // url이 완전히 세팅될때까지 3초 sleep
                val url = webDriver.currentUrl
                val content = webDriver.pageSource

                // 새 탭 닫고 이전 탭으로 이동
                webDriver.close()
                webDriver.switchTo().window(currentWindow)

                // 이미 있는 글을 만나면 더이상 탐색하지 않는다
                if (articleUrls.contains(url)) {
                    break
                }

                log.info("- 제목 : ${title}, url : ${url}")
                articles.add(Article(title = title, url = url, content = content))
            }
            return ScrapResult(articles, null)
        } catch (e: Exception) {
            log.error("블로그 크롤링 실패. blog.url=${blogUrl}", e)
            return ScrapResult(articles, e)
        } finally {
            initializeWebDriver()
        }
    }

    override fun test(url: String): String? {
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

    override fun endUse() {
        webDriver.quit()
    }

    private fun scrollToBottom() {
        val jsExecutor = webDriver as JavascriptExecutor
        jsExecutor.executeScript("window.scrollTo(0, document.body.scrollHeight);")
    }

    // 새 탭만 남기고 모두 닫는다
    private fun initializeWebDriver() {
        webDriver.switchTo().newWindow(WindowType.TAB)
        val newTab = webDriver.windowHandle
        webDriver.windowHandles
            .filter { tab -> tab != newTab }
            .forEach { tab ->
                webDriver.switchTo().window(tab)
                webDriver.close()
            }
        webDriver.switchTo().window(newTab)
    }
}

private fun WebElement.openNewTab(webDriver: WebDriver) {
    val actions = Actions(webDriver)
    val modifierKey =
        if (System.getProperty("os.name").contains("Mac")) Keys.COMMAND else Keys.CONTROL
    actions.keyDown(modifierKey).click(this).keyUp(modifierKey).build().perform()
}
