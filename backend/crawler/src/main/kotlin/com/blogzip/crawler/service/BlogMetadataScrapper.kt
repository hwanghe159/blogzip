package com.blogzip.crawler.service

import com.blogzip.crawler.config.SeleniumProperties
import com.blogzip.crawler.dto.BlogMetadata
import com.blogzip.crawler.undetectedchromedriver.ChromeDriverBuilder
import com.blogzip.crawler.vo.MediumUrl
import com.blogzip.crawler.vo.VelogUrl
import com.blogzip.logger
import io.github.bonigarcia.wdm.WebDriverManager
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.WindowType
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient
import java.net.URI
import java.time.Duration

class BlogMetadataScrapper(
    private val webDriver: WebDriver,
    private val webClient: WebClient,
) {

    val log = logger()

    companion object {
        private val CLIENT_TIMEOUT = Duration.ofSeconds(5)
        private val RSS_POSTFIX = listOf("/rss", "/feed", "/rss.xml", "/feed.xml")
        private val RSS_CONTENT_TYPE =
            setOf("application/xml", "application/rss+xml", "application/atom+xml", "text/xml")

        fun create(properties: SeleniumProperties): BlogMetadataScrapper {
            val webDriverManager = WebDriverManager.chromedriver()
            webDriverManager.setup()
            val driverHome = webDriverManager.downloadedDriverPath
            val chromeOptions = ChromeOptions()
            chromeOptions.addArguments(properties.chromeOptions)
            val webDriver = ChromeDriverBuilder()
                .build(chromeOptions, driverHome)

            return BlogMetadataScrapper(
                webDriver,
                WebClient.builder()
                    .exchangeStrategies(
                        ExchangeStrategies.builder()
                            .codecs { it.defaultCodecs().maxInMemorySize(-1) }
                            .build()
                    ).build()
            )
        }
    }

    @Synchronized
    fun getMetadata(url: String): BlogMetadata {
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
            // todo rss에 요청한번 보내보고 4XX, 5XX이면 null 응답
            return BlogMetadata(title = title, imageUrl = imageUrl, rss = rss)
        } catch (e: Exception) {
            val metadata = BlogMetadata(title = title!!, imageUrl = imageUrl, rss = rss)
            log.error("${url}에서 메타데이터 추출 중 예외 발생. metadata=${metadata}", e)
            return metadata
        } finally {
            initializeWebDriver()
        }
    }

    fun endUse() {
        webDriver.quit()
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