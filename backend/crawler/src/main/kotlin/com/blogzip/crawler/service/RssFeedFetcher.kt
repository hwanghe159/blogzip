package com.blogzip.crawler.service

import com.blogzip.logger
import com.blogzip.crawler.dto.Article
import com.rometools.rome.feed.synd.SyndEntry
import com.rometools.rome.io.SyndFeedInput
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.io.BufferedReader
import java.io.StringReader
import java.time.ZoneId

// contents 또는 description 이 500자 이하인 경우, 요약본으로 판단.
private val SyndEntry.content: String?
    get() {
        var result: String
        if (this.contents.isEmpty()) {
            if (this.description.value.length <= 500) {
                return null
            }
            result = this.description.value
        } else {
            val content = this.contents[0].value
            if (content.length <= 500) {
                return null
            }
            result = content
        }

        val cDataRegex = "<!\\[CDATA\\[(.*?)]]>".toRegex(setOf(RegexOption.DOT_MATCHES_ALL))
        result = cDataRegex.find(result)?.groups?.get(1)?.value ?: result
        return result
    }

class RssFeedFetcher private constructor(
    private val xmlWebClient: WebClient,
) {

    val log = logger()

    companion object {
        fun create(): RssFeedFetcher {
            return RssFeedFetcher(
                xmlWebClient(),
            )
        }

        private fun xmlWebClient(): WebClient {
            return WebClient.builder()
                .defaultHeaders { headers ->
                    headers.setAll(
                        mapOf(
                            HttpHeaders.CONTENT_TYPE to MediaType.APPLICATION_RSS_XML_VALUE,
                            HttpHeaders.USER_AGENT to "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.0.0 Safari/537.36",
                        )
                    )
                }
                .exchangeStrategies(
                    ExchangeStrategies.builder()
                        .codecs { it.defaultCodecs().maxInMemorySize(-1) }
                        .build()
                ).build()
        }
    }

    fun isContentContainsInRss(rss: String): Boolean {
        val articles: List<Article>

        try {
            articles = getArticles(rss)
        } catch (e: Exception) {
            return false
        }

        if (articles.isEmpty()) {
            return false
        }
        if (articles[0].content == null) {
            return false
        }
        return true
    }

    fun getArticles(rss: String): List<Article> {
        val xmlString = xmlWebClient
            .get()
            .uri(rss)
            .exchangeToMono { response ->
                if (response.statusCode().is3xxRedirection) {
                    val newUrl = response.headers().header("Location").firstOrNull()
                    if (newUrl != null) {
                        xmlWebClient.get()
                            .uri(newUrl)
                            .retrieve()
                            .bodyToMono(String::class.java)
                    } else {
                        Mono.error(RuntimeException("rss url로부터 리다이렉트 응답이 왔으나 Location 헤더값이 없음"))
                    }
                } else {
                    response.bodyToMono(String::class.java)
                }
            }
            .block()

        // XML 에서 허용되지 않는 유니코드 문자 제거
        val validXmlString = xmlString!!.replace(
            "[^\\u0009\\r\\n\\u0020-\\uD7FF\\uE000-\\uFFFD\\u10000-\\u10FFFF]".toRegex(),
            ""
        )

        return convertToArticles(validXmlString)
    }

    private fun convertToArticles(xml: String): List<Article> {
        val input = SyndFeedInput()
        val entries = input.build(BufferedReader(StringReader(xml))).entries
        val articles = entries.map {
            Article(
                title = it.title,
                content = it.content,
                url = it.link,
                createdDate = it.publishedDate?.toInstant()
                    ?.atZone(ZoneId.systemDefault())
                    ?.toLocalDate()
            )
        }
        return articles
    }
}